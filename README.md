# Overview
This repository contains a lab for the [mu](https://github.com/stelligent/mu) workshop.  The goal of this lab is to add service discovery for microservice to microservice commnication based on the service from the [first lab](https://github.com/stelligent/mu-workshop-lab1).

![Architecture Diagram](architecture.png)


# Continue from lab 2
This lab assumes that you have a pipeline successfully deployed in the first and second labs.  You can see a completed version of the second lab on the [solution](https://github.com/stelligent/mu-workshop-lab2/tree/solution) branch.

# Limit to 1 instance
Since we are running an in-memory database that is not shared between all instances of the banana service, we need to update it to only run a single instance.  Update the `mu.yml` file and set the `desiredCount` to `1`.  For more details, see the [Services](https://github.com/stelligent/mu/wiki/Services#configuration) section of the wiki.

# Enable Consul
Update the `mu.yml` file in your `banana-service` to enable Consul service discovery for the dev and production environments.  Details can be found in the [mu wiki](https://github.com/stelligent/mu/wiki/Service-Discovery)

Commit and push the change to apply the change:

```
git add --all
git commit -m "enable consul"
git push
```

# Pull down milkshake service
Let's start by pulling down a second spring boot microservice:

```
git clone git@github.com:stelligent/mu-workshop-lab3.git milkshake-service

cd milkshake-service
```

Now, let's create a CodeCommit repo in our AWS account and push the service to the new CodeCommit repo.  If you are working a region other than **us-east-1**, then be sure to update the URL for the CodeCommit repo:

```
aws codecommit create-repository --repository-name milkshake-service

git remote set-url origin https://git-codecommit.us-east-1.amazonaws.com/v1/repos/milkshake-service
```

# Initialize mu.yml
We want to setup our `mu.yml` file so that mu can manage our service.  The easiest way to start is with `mu init`.  This doesn't actually do anything in your AWS account, just in your local mu.yml file.  Do NOT pass the `--env` flag since we are going to share the same environment that is being managed by the `banana-service` from Lab 1.

```
mu init
```

This generates 2 new files, `mu.yml` and `buildspec.yml`.  We need to make some updates to both to reflect run our microservice.

# Configure service
Use the [Services](https://github.com/stelligent/mu/wiki/Services#configuration) section of the wiki to configure the following for your service:

* Path pattern: update to only route `/milkshakes*`

# Configure pipeline
Use the [Pipelines](https://github.com/stelligent/mu/wiki/Pipelines#configuration) section of the wiki to configure the following for your service pipeline:

* Build image: Update the [image](http://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref.html#build-env-ref-available) that CodeBuild uses to `aws/codebuild/java:openjdk-8`

# Configure CodeBuild
Update the [buildspec.yml](http://docs.aws.amazon.com/codebuild/latest/userguide/build-spec-ref.html#build-spec-ref-syntax) to run `gradle build`.

#  Pipeline
Now that our service is ready to test, let's update the pipeline for the service:
https://github.com/stelligent/mu/wiki/Service-Discovery

```
mu pipeline up
```

Finally, push the changes to trigger a pipeline invocation:

```
git add --all
git commit -m "mu testing"
git push
```


# Explore and wait...
While we wait for the pipeline to run, let's explore what mu created:

* **CloudFormation console** - Whats's new?
    * Navigate to the CloudFormation console.  What's included in the new `mu-consul-dev` stack?

* **ECS console** - Whats's new?
    * Check out the new `mu-consul-dev` cluster.  What tasks are running?  How many ECS container instances?  Why?
    * Check out the prior `mu-cluster-dev` cluster.  How many tasks are running?  What containers are running for the `consul-agent` task?

* **mu cli** - Environment logs
    * Use `mu` to search the logs for Consul activity in last 30 minutes for the `dev` environment: `mu env logs -t 30m dev consul`

* **Consul UI** -
    * Get the bastion host and ECS instance IP via `mu env show dev`
    * Login to bastion host `ssh -i ~/.ssh/id_rsa ec2-user@<bastion-ip>`
    * Get the URL for the Consul UI:
    ```aws cloudformation describe-stacks --stack-name mu-consul-dev --query "Stacks[].Outputs[?OutputKey=='ConsulUiUrl'].OutputValue" --output text```
    * Configure your SOCKS5 proxy to `localhost:8080`
    * Navigate to `http://<Consul UI ELB>/ui/`
    * If unable to configure proxy, try curl and notice how many of each server is running.  What is the `ServicePort`?

```
curl -x socks5h://localhost:8080 http://<Consul UI ELB>/v1/catalog/service/milkshake-service | jq
curl -x socks5h://localhost:8080 http://<Consul UI ELB>/v1/catalog/service/banana-service | jq
```

* **Test the service** - Once the pipeline is successfully deployed to the **dev** environment, test it!
    * Get the base URL of the ELB via `mu env show dev`
    * Create a new vanilla milkshake: `curl <baseurl>/milkshakes -F flavor=Vanilla`
    * Create a new banana milkshake: `curl <baseurl>/milkshakes -F flavor=Banana`
    * Create some bananas: `curl <baseurl>/bananas -d "{}" -H "Content-Type: application/json"
    * Create a new banana milkshake: `curl <baseurl>/milkshakes -F flavor=Banana`
