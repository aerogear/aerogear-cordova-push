using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AeroGear.Push
{
    public class PushReceivedEvent : EventArgs
    {
        public PushReceivedEvent(PushNotification Args)
        {
            this.Args = Args;
        }

        public PushNotification Args { get; set; }
    }
}
