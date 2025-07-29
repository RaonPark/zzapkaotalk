package com.example.userservice.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class MailService(
    private val javaMailSender: JavaMailSender,
) {
    fun sendVerificationCodeForRegister(toEmailAddress: String) {
        val message = SimpleMailMessage()
        val verificationCode = generateVerificationCode()

        message.setTo(toEmailAddress)
        message.subject = "Verification Code From Zzapkaotalk"
        message.from = "dont-reply@zzapkaotalk.com"
        message.text = "ZzapKaotalk에서 인증번호가 도착했습니다.\n인증번호\t:\t$verificationCode"

        javaMailSender.send(message)
    }

    private fun generateVerificationCode(): String {
        val random = SecureRandom()
        random.setSeed(System.currentTimeMillis())

        var verificationCode = ""
        repeat(6) {
            verificationCode += random.nextInt()
        }

        return verificationCode
    }
}