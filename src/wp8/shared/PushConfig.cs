using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AeroGear.Push
{
    public class PushConfig
    {
        public Uri UnifiedPushUri { get; set; }

        public string VariantId { get; set; }

        public string VariantSecret { get; set; }

        public IList<string> Categories { get; set; }

        public string Alias { get; set; }
    }
}
