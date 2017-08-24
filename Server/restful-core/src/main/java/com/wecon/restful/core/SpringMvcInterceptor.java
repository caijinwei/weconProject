package com.wecon.restful.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class SpringMvcInterceptor implements HandlerInterceptor
{
	private static final Logger logger = LogManager.getLogger(SpringMvcInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		logger.debug("restful preHandle ...");
		long curr = System.currentTimeMillis();

		// encoding
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		// origin
		String origin = request.getHeader("Origin");
		if (origin != null && !origin.isEmpty())
		{
			response.addHeader("Access-Control-Allow-Origin", origin);
		}

		// header
		if (request.getMethod().toUpperCase().equals("OPTIONS"))
		{
			// header option
			String headers = request.getHeader("Access-Control-Request-Headers");
			if (headers != null && !headers.isEmpty())
			{
				response.addHeader("Access-Control-Allow-Headers", headers);
			}
			return true;
		}
		//websocket
		String baseUrl = request.getRequestURI();
		int begin = baseUrl.indexOf("/api/");
		baseUrl = baseUrl.substring(begin + 4);
		if(baseUrl.contains("-websocket")){
			logger.debug("websocket request");
			return true;
		}

		// 初始化会话上下文
		Session session = new Session(request, response);
		AppContext.setSession(session);

		logger.debug("interceptor coast " + (System.currentTimeMillis() - curr));

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception
	{
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception
	{
	}
}
