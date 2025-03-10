package com.zanddemo.creditcard.test;

import com.alibaba.fastjson.JSON;
import com.zanddemo.creditcard.controller.CreditCardController;
import com.zanddemo.creditcard.controller.DemoAssistantController;
import com.zanddemo.creditcard.entity.CreditCardApplicationBasicInfo;
import com.zanddemo.creditcard.entity.ScoreBoard;
import com.zanddemo.creditcard.service.LoginTokenService;
import com.zanddemo.creditcard.valueobject.ApiResult;
import com.zanddemo.creditcard.valueobject.MockApproval;
import com.zanddemo.creditcard.valueobject.MockSubmission;
import com.zanddemo.creditcard.valueobject.UserBase;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class DemoTest {
    @Autowired
	private DemoAssistantController demoAssistantController;

    @Autowired
	private CreditCardController creditCardController;

    private static final String mockSubmission = "{"
		+ "     \"identity\": true,"
		+ "     \"behaviorAnalysis\": true,"
		+ "     \"compliance\": true,"
		+ "     \"employment\": true,"
		+ "     \"risk\": true"
		+ " }";

    private static final String mockApproval = "{"
		+ "    \"emiratesId\": \"440411\","
		+ "    \"behaviorAnalysis\": 0.4,"
		+ "    \"compliance\": true,"
		+ "    \"employment\": true,"
		+ "    \"risk\": 0.3"
		+ "}";

	@Test
	public void testFullPath() throws InterruptedException {
		System.out.println("start test...");
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		//login
		System.out.println("logging in...");
		UserBase userBase = new UserBase();
		userBase.setEmiratesId("440411");
		userBase.setName("Brian");
		ApiResult loginResult = demoAssistantController.loginToDemoEnv(userBase);
		System.out.println("loginResult: " + loginResult);

		System.out.println("mocking the readiness of services...");
		ApiResult mockResult = demoAssistantController.updateRpcMockSubmissionSetting(JSON.parseObject(mockSubmission, MockSubmission.class));
		System.out.println("mockResult: " + mockResult);

		//create request
		request.addHeader(LoginTokenService.X_CC_TOKEN, loginResult.getData());
		System.out.println("creating request...");
		CreditCardApplicationBasicInfo applicationRequest = new CreditCardApplicationBasicInfo();
		applicationRequest.setEmiratesId("440411");
		applicationRequest.setName("Brian");
        applicationRequest.setMobileNumber("1231239092");
        applicationRequest.setNationality("China");
        applicationRequest.setAddress("dhid ahlkpadf hohad a jioh");
        applicationRequest.setAnnualIncome(new BigDecimal(10000));
        applicationRequest.setEmploymentDetails("asdfiojapsdifj ioaspdjfio pajsdf noiaandfop nasd nopakna opfnasd");
        applicationRequest.setRequestedCreditLimit(new BigDecimal(900000));
		ApiResult createResult = creditCardController.saveCreditCardApplication(applicationRequest);
		System.out.println("createResult: " + createResult);

		//update request
		System.out.println("updating request...");
		applicationRequest.setAddress("asdfpasjdfiophoa jiojiofadf");
		ApiResult updateResult = creditCardController.saveCreditCardApplication(applicationRequest);
		System.out.println("updateResult: " + updateResult);

		//submit for review
		TimeUnit.SECONDS.sleep(1);
		System.out.println("submitting request...");
		ApiResult submitResult = creditCardController.submitCreditCardApplication();
		System.out.println("submitResult: " + submitResult);

		//approve the request
		TimeUnit.SECONDS.sleep(10);
		System.out.println("approving request...");
		demoAssistantController.updateRpcMockApprovalSetting(JSON.parseObject(mockApproval, MockApproval.class));

		//check final result
		ApiResult<ScoreBoard> scoreBoardApiResult = creditCardController.getCreditCardApplicationScore();
		System.out.println("scoreBoardApiResult: " + scoreBoardApiResult);

		ApiResult<CreditCardApplicationBasicInfo> latestApplicationResult = creditCardController.getCreditCardApplication();
		System.out.println("latestApplicationResult: " + latestApplicationResult);

		//logout
		TimeUnit.SECONDS.sleep(10);
		System.out.println("logging out...");
		demoAssistantController.logoutFromDemoEnv();
		System.out.println("logged out, end test.");
	}

}
