package com.wecon.box.test;

import com.wecon.restful.test.TestBase;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Created by zengzhipeng on 2017/8/11.
 */
public class UserActionTest extends TestBase {
	@Test
	public void signin()
	{
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/user/signin");
		request.param("alias", "zzp");
		request.param("password", DigestUtils.md5Hex("1"));
		request.param("isremeber", "0");
		System.out.println(test(request, true));
	}

	@Test
	public void test()
	{
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/testact/gd");

		System.out.println(test(request, true));
	}
}
