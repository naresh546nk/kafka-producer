pipeline{
  agent any
  stages{
    
    stage("build"){
      steps{
        echo "building the application .."
        echo "adding script "
        script{
            def t=2+20000;
            echo "This is the value of new variable t="+t
        }
      }
    }

    stage("test"){
      steps{
        echo "testing the application .."
      }
    }
    stage("deploy"){
      steps{
       echo "deploying the application .."
      }
    }
  }

}
