function GameCtrl($scope) {
  $scope.name = nameGlobal;

  $scope.toSend = "foo";

  var ws = new WebSocket("ws://localhost:9000/gameConnection");

  ws.onmessage = function(message) {
      console.log("got:", message);
      console.log(message.data);
//      var data = JSON.parse(message.data);
//      $scope.$apply($scope.messages = data.tags);
    };

  $scope.echo = function() {
      console.log("would send", $scope.toSend);
      ws.send($scope.toSend);
  };

}