/*
# Copyright (c) 2006-2016 Apple Inc. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
*/
package org.bedework.davtester;

import org.bedework.util.misc.ToString;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.String.format;
import static org.bedework.davtester.Manager.RESULT_IGNORED;
import static org.bedework.davtester.XmlUtils.children;
import static org.bedework.davtester.XmlUtils.getYesNoAttributeValue;
import static org.bedework.util.xml.XmlUtil.getAttrVal;
import static org.bedework.util.xml.XmlUtil.nodeMatches;

/**
 * Maintains a list of tests to run as part of a 'suite'.
 */
class Testsuite extends DavTesterBase {
  public boolean ignore;
  private boolean changeuid;

  public List<Test> tests = new ArrayList<>();

  public Testsuite(final Manager manager) {
    super(manager);
  }

  @Override
  public String getKind() {
    return "Testsuite";
  }

  public void parseXML(final Element node) {
    name = getAttrVal(node, XmlDefs.ATTR_NAME);
    ignore = getYesNoAttributeValue(node, XmlDefs.ATTR_IGNORE);
    only = getYesNoAttributeValue(node, XmlDefs.ATTR_ONLY);
    changeuid = getYesNoAttributeValue(node,
                                       XmlDefs.ATTR_CHANGE_UID);
    httpTrace = getYesNoAttributeValue(node, XmlDefs.ATTR_HTTP_TRACE,
                                       false);

    for (var child : children(node)) {
      if (nodeMatches(child, XmlDefs.ELEMENT_REQUIRE_FEATURE)) {
        parseFeatures(child, true);
      } else if (nodeMatches(child,
                             XmlDefs.ELEMENT_EXCLUDE_FEATURE)) {
        parseFeatures(child, false);
      } else if (nodeMatches(child, XmlDefs.ELEMENT_TEST)) {
        var t = new Test(manager);
        t.parseXML(child);
        tests.add(t);
      }
    }
  }

  public TestResult run(final KeyVals testfile,
                        final String label) {
    try {
      if (httpTrace) {
        httpTraceOn();
      }

      return runTestSuite(testfile,
                          format("%s | %s", label, name));
    } finally {
      if (httpTrace) {
        httpTraceOff();
      }
    }
  }

  public TestResult runTestSuite(final KeyVals testfile,
                                 final String label) {
    var resultName = name;
    var res = new TestResult();
    // POSTGRES postgresCount = null;

    if ((manager.currentTestfile.only && !only) || ignore) {
      manager.testSuite(testfile, resultName,
                        "    Deliberately ignored",
                        RESULT_IGNORED);
      res.ignored = tests.size();
    } else if (hasMissingFeatures()) {
      manager.testSuite(testfile, resultName,
                        format("    Missing features: %s", missingFeatures()),
                        RESULT_IGNORED);
      res.ignored = tests.size();
    } else if (hasExcludedFeatures()) {
      manager.testSuite(testfile, resultName,
                        format("    Excluded features: %s", excludedFeatures()),
                        RESULT_IGNORED);
      res.ignored = tests.size();
    } else {
      // POSTGRES postgresCount = postgresInit();
      //if (manager.memUsage) {
      //  start_usage = manager.getMemusage();
      //}
      var etags = new HashMap<String, String>();
      var onlyTests = false;
      for (var test: tests) {
        if (test.only) {
          onlyTests = true;
          break;
        }
      }

      var testsuite = manager.testSuite(testfile, resultName, "", null);
      if (changeuid) {
        manager.serverInfo.newUIDs();
      }

      for (var test: tests) {
        try {
          if (test.httpTrace) {
            httpTraceOn();
          }

          res.add(test.run(testsuite, etags, onlyTests,
                           format("%s | %s", label, test.name)));
        } finally {
          if (test.httpTrace) {
            httpTraceOff();
          }
        }
      }
      /*
            if (manager.memUsage){
              end_usage=manager.getMemusage();
              manager.message("trace","    Mem. Usage: RSS=%s%% VSZ=%s%%"%(str(((end_usage[1]-start_usage[1])*100)/start_usage[1]),str(((end_usage[0]-start_usage[0])*100)/start_usage[0])))
              }
        */
    }

    manager.trace(format("  Suite Results: %d PASSED, %d FAILED, %d IGNORED\n",
                         res.ok, res.failed, res.ignored));
    /* POSTGRES
        if postgresCount is ! null:
            postgresResult(postgresCount, indent=4);
         */
    return res;
  }

  public String toString() {
    var ts = new ToString(this);

    ts.append("name", name);
    for (var test : tests) {
      test.toStringSegment(ts);
    }

    return ts.toString();
  }
}