function GameCtrl($scope) {
  $scope.name = nameGlobal;

  $scope.state;

  $scope.ws = new WebSocket("ws://localhost:9000/gameConnection");

  $scope.ws.onopen = function() {
      $scope.ws.send('connect:' + $scope.name);
  };

  $scope.ws.onmessage = function(message) {
      console.log(message.data);
      var newData = JSON.parse(message.data);
      $scope.$apply($scope.data = newData);
    };

  $scope.echo = function() {
      console.log("would send", $scope.toSend);
      $scope.ws.send($scope.toSend);
  };

}