package jenkins.plugins.slack.workflow;


import hudson.model.Result;
import jenkins.plugins.slack.Messages;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class SlackSendStepIntegrationTest {

    @Test
    void configRoundTrip(JenkinsRule jenkinsRule) throws Exception {
        SlackSendStep step1 = new SlackSendStep();
        step1.setMessage("message");
        step1.setColor("good");
        step1.setChannel("#channel");
        step1.setTeamDomain("teamDomain");
        step1.setBaseUrl("baseUrl");
        step1.setFailOnError(true);
        step1.setIconEmoji(":+1:");
        step1.setUsername("username");

        SlackSendStep step2 = new StepConfigTester(jenkinsRule).configRoundTrip(step1);
        jenkinsRule.assertEqualDataBoundBeans(step1, step2);
    }

    @Test
    void test_global_config_override(JenkinsRule jenkinsRule) throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "workflow");
        //just define message
        job.setDefinition(new CpsFlowDefinition("slackSend(message: 'message', baseUrl: 'baseUrl', teamDomain: 'teamDomain', token: 'token', tokenCredentialId: 'tokenCredentialId', channel: '#channel', color: 'good', iconEmoji: ':+1:', username: 'username', timestamp: '124124.12412');", true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0).get());
        //everything should come from step configuration
        jenkinsRule.assertLogContains(Messages.slackSendStepValues("baseUrl/", "teamDomain", "#channel", "good", false, "tokenCredentialId", false, ":+1:", "username", "124124.12412"), run);
    }

    @Test
    void test_fail_on_error(JenkinsRule jenkinsRule) throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "workflow");
        //just define message
        job.setDefinition(new CpsFlowDefinition("slackSend(message: 'message', baseUrl: 'baseUrl', teamDomain: 'teamDomain', token: 'token', tokenCredentialId: 'tokenCredentialId', channel: '#channel', color: 'good', failOnError: true);", true));
        WorkflowRun run = jenkinsRule.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        //everything should come from step configuration
        jenkinsRule.assertLogContains(Messages.notificationFailed(), run);
    }

    @Test
    void test_fail_on_missing_message(JenkinsRule jenkinsRule) throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "workflow");
        //with a null message
        job.setDefinition(new CpsFlowDefinition("slackSend(message: null, baseUrl: 'baseUrl', teamDomain: 'teamDomain', token: 'token', tokenCredentialId: 'tokenCredentialId', channel: '#channel', color: 'good', failOnError: true);", true));
        jenkinsRule.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
    }
}
