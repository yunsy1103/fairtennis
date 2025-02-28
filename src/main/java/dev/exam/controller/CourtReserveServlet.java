package dev.exam.controller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import dev.exam.model.Reservation;

@WebServlet("/tennis/reserve")
public class CourtReserveServlet extends HttpServlet {
	private static final String RECAPTCHA_SECRET_KEY = "";
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		// reCAPTCHA 검증
		String recaptchaResponse = request.getParameter("g-recaptcha-response");
		if (!verifyRecaptcha(recaptchaResponse)) {
			response.getWriter().write("reCAPTCHA 검증 실패! 봇으로 의심됩니다.");
			return;
		}

		// form 입력 값 추출
		String reservedTimeString = request.getParameter("datetime");
		LocalDateTime reservedTime = LocalDateTime.parse(reservedTimeString, formatter);
		int courtNumber = Integer.parseInt(request.getParameter("court"));
		String centerName = request.getParameter("center");

		// 예매 처리
		Reservation reservation = new Reservation(centerName, courtNumber, reservedTime);
		request.setAttribute("reservation", reservation);

		final String path = "/success.jsp";
		RequestDispatcher dispatcher = request.getRequestDispatcher(path);
		dispatcher.forward(request, response);
	}

	private boolean verifyRecaptcha(String recaptchaResponse) throws IOException {
		String url = "https://www.google.com/recaptcha/api/siteverify?secret=" + RECAPTCHA_SECRET_KEY
			+ "&response=" + recaptchaResponse;
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);

		Scanner scanner = new Scanner(conn.getInputStream());
		String response = scanner.useDelimiter("\\A").next();
		scanner.close();

		JSONObject jsonObject = new JSONObject(response);
		return jsonObject.getBoolean("success");
	}
}
