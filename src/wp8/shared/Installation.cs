using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AeroGear.Push
{
    public class Installation
    {
        public string deviceToken { get; set; }
        public string operatingSystem { get; set; }
        public string osVersion { get; set; }
        public string platform { get; set; }
        public IList<string> categories { get; set; }
        public string alias { get; set; }
    }
}
