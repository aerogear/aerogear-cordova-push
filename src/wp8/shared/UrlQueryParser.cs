using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace AeroGear.Push
{
    public class UrlQueryParser
    {
        public static IDictionary<string, string> ParseQueryString(string url)
        {
            Dictionary<string, string> result = new Dictionary<string, string>();

            url = url.Substring(url.IndexOf('?') + 1);

            foreach (string param in Regex.Split(url, "&"))
            {
                string[] keyValue = Regex.Split(param, "=");
                result.Add(keyValue[0], WebUtility.UrlDecode(keyValue[1]));
            }

            return result;
        }
    }
}
