// Code goes here
(function(){
  
  var MainController = function($scope,$http){
    
    $scope.text ="";    
    $scope.loading = false;    
    $scope.timeTaken =0;
    
    $scope.results = [];
    
    $scope.search = function () {
      var start = new Date().getTime();
      $scope.loading =true;
      $http.get("search.jsp?text=" + $scope.text)
        .then(function success(response) {
        	$scope.timeTaken = new Date().getTime() - start;
            $scope.results = response.data; 
            $scope.loading =false;
          },
          function error(response) {
            return "data couldn't be returned";
          }
      )      
    }
    
    $scope.autoComplete = function (text) {
    	
    	return $http.get("getAllWords.jsp?text=" + text)
	        .then(function success(response) {
	            return response.data;
	          }
	      )

    }
    
    $scope.themes = [{ name: "Cerulean", url: "https://bootswatch.com/cerulean/bootstrap.min.css" },
                     { name: "Cosmo", url: "https://bootswatch.com/cosmo/bootstrap.min.css" },
                     { name: "cyborg", url: "https://bootswatch.com/cyborg/bootstrap.min.css" },
                     { name: "Darkly", url: "https://bootswatch.com/darkly/bootstrap.min.css" },
                     { name: "Flatly", url: "https://bootswatch.com/flatly/bootstrap.min.css" },
                     { name: "Journal", url: "https://bootswatch.com/journal/bootstrap.min.css" },
                     { name: "Lumen", url: "https://bootswatch.com/lumen/bootstrap.min.css" },
                     { name: "Paper", url: "https://bootswatch.com/paper/bootstrap.min.css" },
                     { name: "Readable", url: "https://bootswatch.com/readable/bootstrap.min.css" },
                     { name: "Sandstone", url: "https://bootswatch.com/sandstone/bootstrap.min.css" },
                     { name: "Simplex", url: "https://bootswatch.com/simplex/bootstrap.min.css" },
                     { name: "Slate", url: "https://bootswatch.com/slate/bootstrap.min.css" },
                     { name: "Spacelab", url: "https://bootswatch.com/spacelab/bootstrap.min.css" },
                     { name: "Superhero", url: "https://bootswatch.com/superhero/bootstrap.min.css" },
                     { name: "United", url: "https://bootswatch.com/united/bootstrap.min.css" },
                     { name: "Yeti", url: "https://bootswatch.com/yeti/bootstrap.min.css" }
                 ]
    $scope.selectedTheme = $scope.themes[4];

    $scope.changeTheme = function (theme) {
        $scope.selectedTheme = theme;
    }
  }
  
  var MainApp = angular.module("MainApp",["ngAnimate","ui.bootstrap","ngLoadingSpinner"]);
  MainApp.controller("MainController",["$scope","$http", MainController]);
  
}());
