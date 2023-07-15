package org.example;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.util.Map;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        //DockerClientConfig standard = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
                //.withRegistryUrl("https://mog/notdockerhub")
                .build();


        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();


        DockerHttpClient.Request request = DockerHttpClient.Request.builder()
                .method(DockerHttpClient.Request.Method.GET)
                .path("/_ping")
                .build();


        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        //DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        PullImageResultCallback resultCallback = new PullImageResultCallback();

        dockerClient.pullImageCmd("")
                .withRepository("docker.elastic.co/elasticsearch/elasticsearch:7.10.0")
                .exec(resultCallback).awaitCompletion();






        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd("docker.elastic.co/elasticsearch/elasticsearch:7.10.0")
                .withExposedPorts(ExposedPort.tcp(9200))
                .withEnv("discovery.type=single-node")
                .withHostConfig(HostConfig.newHostConfig().withPortBindings(PortBinding.parse("9200:9200")));

        String containerId =  createContainerCmd.exec().getId();

        dockerClient.startContainerCmd(containerId).exec();

        dockerClient.pullImageCmd("docker.elastic.co/elasticsearch/elasticsearch:7.10.0").start();

    }
}