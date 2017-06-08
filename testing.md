In the jetty.project root, run the following ...

    $ mvn generate-resources 

Or with deploy testing (locally)

    $ mvn deploy -Dtest=None \
         -DaltDeploymentRepository=tmp::default::file:/tmp/repo/


