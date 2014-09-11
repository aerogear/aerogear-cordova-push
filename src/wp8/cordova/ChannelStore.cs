using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AeroGear.Push
{
    using System.IO.IsolatedStorage;

    public class ChannelStore
    {
        private const string STORE_KEY = "Channel";

        public void Save(string channel)
        {
            OpenSettings().Add(STORE_KEY, channel);
        }

        public string Read()
        {
            IsolatedStorageSettings settings = OpenSettings();
            return settings.Contains(STORE_KEY) ? (string) settings[STORE_KEY] : null;
        }

        private IsolatedStorageSettings OpenSettings()
        {
            return IsolatedStorageSettings.ApplicationSettings;
        }
    }
}
