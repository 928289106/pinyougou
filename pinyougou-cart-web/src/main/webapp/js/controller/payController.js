app.controller('payController',function ($scope,$location,payService) {

    $scope.createNative = function () {
        payService.createNative().success(
            function (response) {
                $scope.out_trade_no=response.out_trade_no;
                $scope.tatol_fee=(response.tatol_fee/100).toFixed(2);

                var qr = new QRious({
                    element:
                        document.getElementById('qrious'),
                    size:250,
                    level:'H',
                    value:response.code_url
                });

                $scope.queryStatus();
            }
        )
    }

    $scope.queryStatus = function () {
        payService.queryStatus($scope.out_trade_no).success(
            function (response) {
                if(response.success){
                    alert(response.message)
                    window.location.href="paysucess.html#?tatol_fee="+$scope.tatol_fee;
                }else{
                    if(response.message=='支付超时'){
                        alert(response.message);
                        $scope.createNative();
                    }else {
                        alert(response.message);
                    }
                }
            }
        )
    }

    $scope.getMoney = function () {
        var money = $location.search()['money']
        return money
    }

})