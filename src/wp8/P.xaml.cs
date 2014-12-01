using System;
using System.Windows.Navigation;
using Microsoft.Phone.Controls;
using System.Collections.Generic;

namespace org.jboss.aerogear.cordova.push
{
    public partial class P : PhoneApplicationPage
    {
        public static string message;
        public static IDictionary<string, string> data;

        public P()
        {
            InitializeComponent();
        }

        protected override void OnNavigatedTo(NavigationEventArgs e)
        {
            if (NavigationContext.QueryString.ContainsKey("message"))
            {
                message = NavigationContext.QueryString["message"];
                data = NavigationContext.QueryString;
            }

            NavigationService.Navigate(new Uri("/MainPage.xaml", UriKind.Relative));
        }
    }
}