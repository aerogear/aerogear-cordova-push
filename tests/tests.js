/*
 * JBoss, Home of Professional Open Source.
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

exports.defineAutoTests = function () {
  describe('Plugin object', function () {
    it("push plugin should exist", function () {
      expect(push).toBeDefined();
      expect(typeof push == 'object').toBe(true);
    });

    it("should contain a register function", function () {
      expect(push.register).toBeDefined();
      expect(typeof push.register == 'function').toBe(true);
    });

    it("should contain an unregister function", function () {
      expect(push.unregister).toBeDefined();
      expect(typeof push.unregister == 'function').toBe(true);
    });

    it("should contain a setApplicationIconBadgeNumber function", function () {
      expect(push.setApplicationIconBadgeNumber).toBeDefined();
      expect(typeof push.setApplicationIconBadgeNumber == 'function').toBe(true);
    });
  });
};

exports.defineManualTests = function(contentEl, createActionButton) {
  var logMessage = function (message, color) {
        var log = document.getElementById('info');
        var logLine = document.createElement('div');
        if (color) {
            logLine.style.color = color;
        }
        logLine.innerHTML = message;
        log.appendChild(logLine);
    }

    var clearLog = function () {
        var log = document.getElementById('info');
        log.innerHTML = '';
    }

    var html = '<h3>Register with Unified Push Server</h3>' +
        '<div id="output"></div>' +
        '<textarea id="config" rows="6" cols="50">' +
        '{\n' +
          '"pushServerURL": "http://",\n'+
          '"variantID": "",\n'+
          '"variantSecret": ""\n'+
        '}'+
        '</textarea>'
        'Expected result: Status box will show success message';

    contentEl.innerHTML = '<div id="info"></div>' + html;

    createActionButton('Register', function() {
      clearLog();
      var config = JSON.parse(document.getElementById('config').value);
      push.register(function() {}, function() {
        logMessage('Registration successful check console to see installation');
      },function(err) {
        logMessage('Error ' + err, 'red');
      }, config);
    }, "output");
};