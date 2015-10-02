/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace AeroGear.Push
{
    /// <summary>
    /// Parses a query string like ?test=bla&super=cool into a IDictionary.
    /// </summary>
    public class UrlQueryParser
    {
        public static IDictionary<string, string> ParseQueryString(string url)
        {
            url = url.Substring(url.IndexOf('?') + 1);

            return Regex.Split(url, "&").Select(param => Regex.Split(param, "=")).ToDictionary(keyValue => keyValue[0], keyValue => WebUtility.UrlDecode(keyValue[1]));
        }
    }
}
