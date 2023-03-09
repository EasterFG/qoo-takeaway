package com.easterfg.takeaway.handler;

import com.alipay.api.AlipayApiException;
import com.easterfg.takeaway.dto.Result;
import com.easterfg.takeaway.exception.AccessDeniedException;
import com.easterfg.takeaway.exception.BusinessException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static com.easterfg.takeaway.utils.ErrorCode.*;

/**
 * @author EasterFG on 2022/9/24
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数验证异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result paramError(BindException exception) {
        Map<String, String> map = new HashMap<>();
        BindingResult result = exception.getBindingResult();
        result.getFieldErrors().forEach(error -> map.put(error.getField(), error.getDefaultMessage()));
        return Result.failed("40002", "请求参数错误", map);
    }

    /**
     * Jwt解析异常
     */
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result jwt(JwtException e, HttpServletResponse response) {
        // 未经授权的访问
        response.setStatus(401);
        if (e instanceof SignatureException) {
            return Result.failed(AUTHORIZATION_SIGNATURE_EXCEPTION);
        } else if (e instanceof ExpiredJwtException) {
            return Result.failed(AUTHORIZATION_EXPIRED);
        }
        return Result.failed(NOT_LOGGED_IN);
    }

    /**
     * 文件无法找到
     */
    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result fileNotFound() {
        return Result.failed(FILE_NOT_FOUND);
    }

    /**
     * 文件大小超过限制
     */
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result multipart() {
        return Result.failed(FILE_SIZE_OVERRUN);
    }

    /**
     * 参数解析异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result typeException() {
        return Result.failed(USER_REQUEST_PARAMETER_ERROR);
    }

    /**
     * 支付宝api异常
     */
    @ExceptionHandler(AlipayApiException.class)
    public Result alipayApiException(AlipayApiException exception) {
        log.error("第三方服务异常 {}", exception.getMessage());
        return Result.failed("B1001", "第三方支付API出现异常, 请稍后再试");
    }

    /**
     * 用户权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result noPermissionException(AccessDeniedException exception) {
        return Result.failed("40006", exception.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result businessException(BusinessException exception) {
        return Result.failed(exception.getCode(), exception.getMessage());
    }
}
