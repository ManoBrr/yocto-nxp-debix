FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:imx8mp-debix-model-ab = " file://0001-debix-io-board-gpio-mux.patch"
