package com.example.service

import com.android.apksig.ApkSigner
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.Date

object ApkSignerHelper {

    private var cachedKeyPair: KeyPair? = null
    private var cachedCert: X509Certificate? = null

    @Synchronized
    fun getOrCreateSigningKey(): Pair<PrivateKey, X509Certificate> {
        if (cachedKeyPair != null && cachedCert != null) {
            return Pair(cachedKeyPair!!.private, cachedCert!!)
        }

        val keyPairGen = KeyPairGenerator.getInstance("RSA")
        keyPairGen.initialize(2048)
        val keyPair = keyPairGen.generateKeyPair()

        val notBefore = Date(System.currentTimeMillis() - 24L * 3600 * 1000)
        val notAfter = Date(System.currentTimeMillis() + 30L * 365 * 24 * 3600 * 1000) // 30 years
        val serial = BigInteger.valueOf(System.currentTimeMillis())
        val name = X500Name("CN=BetterCraft, OU=Luanti, O=VoxelCraft, C=BR")

        val certBuilder = JcaX509v3CertificateBuilder(
            name,
            serial,
            notBefore,
            notAfter,
            name,
            keyPair.public
        )

        val signer = JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private)
        val certHolder = certBuilder.build(signer)
        val cert = JcaX509CertificateConverter().getCertificate(certHolder)

        cachedKeyPair = keyPair
        cachedCert = cert

        return Pair(keyPair.private, cert)
    }

    fun signApk(
        unsignedApk: File,
        signedApk: File,
        onProgress: (String) -> Unit = {}
    ) {
        onProgress("Gerando par de chaves e certificado RSA 2048-bit...")
        val (privateKey, cert) = getOrCreateSigningKey()

        onProgress("Configurando assinador APK v1, v2 e v3...")
        val signerConfig = ApkSigner.SignerConfig.Builder(
            "BETTERCRAFT",
            privateKey,
            listOf(cert)
        ).build()

        onProgress("Iniciando assinatura digital do APK...")
        val signer = ApkSigner.Builder(listOf(signerConfig))
            .setInputApk(unsignedApk)
            .setOutputApk(signedApk)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .build()

        signer.sign()
        onProgress("APK assinado com sucesso via APK Signature Scheme v1+v2+v3!")
    }
}
