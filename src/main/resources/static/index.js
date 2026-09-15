
const amount = {
  currency: "KRW",
  value: 36500,          // 주문 금액
};

const orderId = "U3mmuz9pp5XUROCxWaLo";   // 주문번호

const main = async () => {
  const tossPayments = TossPayments(
    "test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm"
  );
  const widgets = tossPayments.widgets({ customerKey: TossPayments.ANONYMOUS });

  await widgets.setAmount(amount);

  const [paymentMethodWidget] = await Promise.all([
    widgets.renderPaymentMethods({
      selector: "#payment-method",
      variantKey: "DEFAULT",
    }),
    widgets.renderAgreement({ selector: "#agreement", variantKey: "AGREEMENT" }),
  ]);

  const paymentRequestButton = document.getElementById('payment-request-button');

  paymentRequestButton.addEventListener('click', async () => {
    try {
      await widgets.requestPayment({
        orderId: orderId,
        orderName: "패션커머스 주문",
        successUrl: window.location.origin + "/success.html",
        failUrl: window.location.origin + "/fail.html",
        customerEmail: "customer123@gmail.com",
        customerName: "김토스",
        customerMobilePhone: "01012341234",
      });
    } catch (err) {
      console.log('err', err);
    }
  });
}

main();