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

import org.bedework.util.misc.Util;

public class Utils {
  public static String upperFirst(String val) {
    if ((val == null) || (val.length() == 0)) {
      return val;
    }

    var first = String.valueOf(Character.toUpperCase(val.charAt(0)));

    if (val.length() == 1) {
      return first;
    }

    return first + val.substring(1);
  }

  /** This can log the message
   *
   * @param message to display
   */
  public static void throwException(final String message) {
    throw new RuntimeException(message);
  }

  /** This can log the message
   *
   * @param t exception
   */
  public static void throwException(final Throwable t) {
    throw new RuntimeException(t);
  }

  public static class KeyValsPropertyFetcher implements Util.PropertyFetcher {
    private final KeyVals props;

    public KeyValsPropertyFetcher(KeyVals props) {
      this.props = props;
    }

    public String get(String name) {
      var val = this.props.get(name);

      if (Util.isEmpty(val)) {
        return null;
      }

      var val0 = val.get(0);
      if (val0 instanceof String) {
        return (String)val0;
      }
      return null;
    }
  }
}

/*
public void processHrefSubstitutions(hrefs, prefix) {
    """
    Process the list of hrefs by prepending the supplied prefix. If the href is a
    list of hrefs, then prefix each item in the list and expand into the results. The
    empty string is represented by a single "-" in an href list.

    @param hrefs: list of URIs to process
    @type hrefs: L{list} of L{str}
    @param prefix: prefix to apply to each URI
    @type prefix: L{str}

    @return: resulting list of URIs
    @rtype: L{list} of L{str}
    """

    results = []
    for href in hrefs:
        if href.startsWith("[") {
            children = href[1:-1].split(",")
            results.extend([(prefix + (i if i != "-" } else "")).rstrip("/") for i in children if i])
        } else {
            results.append((prefix + href).rstrip("/"))

    return results
*/