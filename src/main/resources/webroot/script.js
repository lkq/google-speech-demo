
    var BUFF_SIZE = 16384;
    var audioContext;
    var sessionID, sequence;

    function sendRequest(url, samples, retryCount, onSuccess) {
      if (retryCount <= 0) {
        console.info('reach max retry, stopStreamingAudio sending request: ' + url);
        return;
      }
      var httpRequest = new XMLHttpRequest();
      httpRequest.onerror = function () {
        console.log('network error, failed to send request: ' + url);
        sendRequest(url, samples, retryCount - 1, onSuccess);
      }
      httpRequest.onreadystatechange = function () {
        if (httpRequest.readyState === 4) {
          if (httpRequest.status == 200) {
            if (onSuccess) {
              onSuccess(httpRequest);
            }
          } else if (httpRequest.status != 500){
            // do not retry if get 500 response.
            console.log('failed to send request: ' + url);
            sendRequest(url, samples, retryCount - 1);
          }
        }
      }

      console.log('sending request' + url + ', remaining retries=' + retryCount);
      httpRequest.open('PUT', url, true);
      httpRequest.send(samples);
    }

    function float32ToInt16(samples) {
      var index = samples.length;
      var int16Buffer = new Int16Array(index);
      var clippedValue;
      while (index--) {
        clippedValue = Math.max(-1, Math.min(1, samples[index]))
        int16Buffer[index] = clippedValue > 0 ? clippedValue * 0x7FFF : clippedValue * 0x8000
      }
      return int16Buffer;
    }

    function startStreamingAudio() {
      toggleButtons(false);
      togglePlaceholder(false);
      if (audioContext) {
        console.info('already recording...');
        return;
      }
      audioContext = new (window.AudioContext || window.webKitAudioContext)();
      navigator.webkitGetUserMedia({ audio: true }, function (stream) {
        sessionID = (Math.random() * new Date().getMilliseconds()).toString(36);
        sequence = 0;
        var microphoneSource = audioContext.createMediaStreamSource(stream);
        var streamingProcessor = audioContext.createScriptProcessor(BUFF_SIZE, 1, 1);
        streamingProcessor.onaudioprocess = function (audioProcessingEvent) {
          var samples = audioProcessingEvent.inputBuffer.getChannelData(0);
          console.info('processing samples from microphone, length=' + samples.length);
          var int16Buffer = float32ToInt16(samples)
          var url = '/buffer/' + sessionID + '?sequence=' + sequence++;
          sendRequest(url, int16Buffer, 5, null);
        };
        microphoneSource.connect(streamingProcessor);
        streamingProcessor.connect(audioContext.destination);
      },
        function (error) {
          console.error('unable to access audio device:' + error);
        })
    }

    function stopStreamingAudio() {
      toggleButtons(true);
      togglePlaceholder(true);
      if (sessionID) {
        var url = '/recognize/' + sessionID + '?sequence=' + sequence + '&finished=true&sampleRate=' + audioContext.sampleRate
        sessionID = undefined;
        sequence = undefined;
        sendRequest(url, null, 5, function (httpRequest) {
          var resultBox = document.getElementById('result')
          resultBox.innerText = httpRequest.response;
        });
      }
      if (audioContext) {
        audioContext.close();
        audioContext = undefined;
      }
    }

    function toggleButtons(enableStartBtn) {
      if (!!enableStartBtn) {
        document.getElementById("startBtn").disabled = undefined;
        document.getElementById("stopBtn").disabled = "disabled";
      } else {
        document.getElementById("startBtn").disabled = "disabled";
        document.getElementById("stopBtn").disabled = undefined;
      }
    }

    function togglePlaceholder(show) {
      var resultBox = document.getElementById("result");
      if (!!show) {
        resultBox.innerText = "(recognizing...)";
      } else {
        resultBox.innerText = "";
      }
    }