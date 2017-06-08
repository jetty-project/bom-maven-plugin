In the jetty.project root, run the following ...

    $ mvn generate-resources -rf :test-jetty-osgi-server

Or with deploy testing (locally)

    $ mvn generate-resources deploy -Dtest=None \
         -DaltDeploymentRepository=tmp::default::file:/tmp/repo/ \
         -rf :test-jetty-osgi-server


