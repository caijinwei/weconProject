package com.wecon.restful.core;

import com.wecon.common.redis.RedisManager;

import java.util.Set;

/**
 * SessionManager
 */
public class SessionManager
{
	private static final String RedisGroupName = "session";
	private static final String RedisKey = "UserSession:Guid:%s";
	private static final String UserSidGroupKey = "UserSession:UserID:%s";

	/**
	 * redis会话保持
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @sessionId 会话ID
	 * @userInfo 用户实体
	 */
	public static boolean persistSession(String sessionId, SessionState.UserInfo userInfo, int seconds)
	{
		if (userInfo != null)
		{
			// 1. sid -> userID
			String sessionRedisKey = java.lang.String.format(RedisKey, sessionId);
			RedisManager.set(RedisGroupName, sessionRedisKey.getBytes(), userInfo.toByteArray(), seconds);
			// 2. userID - sid集合
			String userSidGroupRedisKey = String.format(UserSidGroupKey, userInfo.getUserID());
			RedisManager.sadd(RedisGroupName, userSidGroupRedisKey, sessionId);
			return true;
		}
		return false;
	}

	/***
	 * 刷新SID缓存时间
	 * @param sessionId
	 * @param seconds
	 * @return
	 */
	public static boolean expireSid(String sessionId, int seconds)
	{
		if (!sessionId.isEmpty())
		{
			String sessionRedisKey = java.lang.String.format(RedisKey, sessionId);
			return RedisManager.expire(RedisGroupName, sessionRedisKey, seconds);
		}
		return false;
	}

	/***
	 * 删除单个会话信息
	 * @param sessionId 会话ID
	 * @return
	 */
	public static boolean deleteSession(String sessionId)
	{
		if (sessionId != null && !sessionId.isEmpty())
		{
			String sessionRedisKey = java.lang.String.format(RedisKey, sessionId);
			return RedisManager.del(RedisGroupName, sessionRedisKey);
		}
		return false;
	}

	/***
	 * 删除所有会话信息
	 * @param userId 用户ID
	 * @return
	 */
	public static boolean deleteUserSession(long userId)
	{
		if (userId > 0)
		{
			String userSidGroupRedisKey = String.format(UserSidGroupKey, userId);
			Set<String> sidset = RedisManager.smembers(RedisGroupName, userSidGroupRedisKey);
			if (sidset != null && !sidset.isEmpty())
			{
				// 1.删除所有 sid -> user
				RedisManager.del(RedisGroupName, sidset.toArray(new String[sidset.size()]));
			}
			// 2. 删除userID - sid集合
			return RedisManager.del(RedisGroupName, userSidGroupRedisKey);
		}
		return false;
	}

	/**
	 * 根据用户会话获取用户信息
	 * <p/>
	 * Time complexity: O(1)
	 *
	 * @sessionId 会话ID
	 */
	public static SessionState.UserInfo getUserInfo(String sessionId)
	{
		if (sessionId != null && !sessionId.isEmpty())
		{
			String sessionRedisKey = java.lang.String.format(RedisKey, sessionId);
			byte[] value = RedisManager.get(RedisGroupName, sessionRedisKey.getBytes());
			if (value != null)
			{
				try
				{
					return SessionState.UserInfo.parseFrom(value);
				}
				catch (Exception ex)
				{
					throw new RuntimeException(ex);
				}
			}
		}
		return null;
	}
}
