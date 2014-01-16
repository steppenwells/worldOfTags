function EchoCtrl($scope) {
  $scope.messages = ["one", "two"];

  $scope.toSend = "foo";

  var ws = new WebSocket("ws://localhost:9000/echoSocket");

    ws.onmessage = function(message) {
      console.log("got:", message);
      $scope.$apply($scope.messages.push(message.data));
    };

  $scope.echo = function() {
      console.log("would send", $scope.toSend);
      ws.send($scope.toSend);
  };

}