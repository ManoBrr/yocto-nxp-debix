SUMMARY = "Minimal Debix image with basic services"
DESCRIPTION = "A small console-only image for the Debix Model AB with SSH, networking and GPIO tools."
LICENSE = "MIT"

inherit core-image

IMAGE_FEATURES += "ssh-server-dropbear debug-tweaks"

IMAGE_INSTALL = "packagegroup-core-boot ${CORE_IMAGE_EXTRA_INSTALL}"

CORE_IMAGE_EXTRA_INSTALL += " \
    iproute2 \
    ethtool \
    libgpiod-tools \
    i2c-tools \
    debix-custom \
"

ROOTFS_POSTPROCESS_COMMAND += "remove_debix_expand; "
remove_debix_expand() {
    rm -f ${IMAGE_ROOTFS}/usr/local/polyhex/expand_sd_rootfs.sh
}

# meta-imx-bsp/conf/layer.conf appends jailhouse and optee packages to all
# images via IMAGE_INSTALL:append. Remove them for a minimal rootfs.
IMAGE_INSTALL:remove = "jailhouse packagegroup-fsl-optee-imx"
