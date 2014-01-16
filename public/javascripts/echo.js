function EchoCtrl($scope) {
  $scope.messages = ["one", "two"];

  $scope.toSend = "foo";

  $scope.echo = function() {
      console.log("would send", $scope.toSend);
    // send message
  };

}