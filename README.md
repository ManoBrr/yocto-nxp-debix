# New features
A summary of the main new features is as follows.
New features added for all supported boards:
- Upgraded the kernel to 6.12.3 with consolidated Linux Factory Kernel.
- Upgraded the U-Boot to v2024.04 with consolidated Linux Factory U-Boot.
- Updated EULA to v58 November 2024.
- Upgraded the Yocto Project to version 5.1 Styhead.
- Supports the GCC 14.2 toolchain.
- Supports Glitch Detection (GDET) on i.MX 93.
- Cortex-M33 update for 8ULP and i.MX 93, Cortex-M7 updates for i.MX 8M Nano, i.MX 8M Plus, and i.MX 95,
and Cortex-M4 update for i.MX 7ULP, i.MX 8M Mini, and i.MX 8M Quad.
- Security
  1. OP-TEE upgraded to 4.4.0.
- Multimedia updates:
  1. Enabled the "V4L2VD" video decoder in Chromium 129.0.6668.100.
  2. Camera preview: Supports community capture driver for i.MX 8M Nano, i.MX 8M Mini, i.MX 8M Quad, i.MX8ULP, i.MX 8QuadXPlus, i.MX 8QuadMax, and i.MX 93. This feature is not working for i.MX 8DualX, support for it will be added in the next release.
- Graphics updates:
  1. i.MX 9 GPU driver upgraded to r53p0.
  2. i.MX 6/7/8 GPU driver upgraded to 6.4.11.p3.0 with Vulkan enablement, bug fixes, and performance optimizations.
  3. GPU SDK upgraded to 6.2.4.
  4. Chromium upgraded to 129.0.6668.100.
> **Note:** Chromium is not supported on i.MX 6 and i.MX 7 any longer due to the DRM/KMS display driver limitation, but is still supported on i.MX 8 and i.MX 9.
- Machine Learning updates:
  1. OpenCV upgraded to 4.10.0.
  2. TensorFlow Lite upgraded to 2.16.2 with GPU acceleration.
  3. i.MX 93 Vela upgraded to 3.12.
- i.MX 8M Plus
  1. Updates for ISP 4.2.2.25.1.
- i.MX 91
  1. Introduction for i.MX 91 11x11 as GA quality.
  2. Introduction for i.MX 91 9x9 as GA quality.
- Arm SystemReady-IR (SR-IR) certification
  1. i.MX 8M Mini EVK board has passed the Arm SR-IR certification.
  2. i.MX 8M Plus EVK board has passed the Arm SR-IR certification.
  3. i.MX 8M Quad EVK board has passed the Arm SR-IR certification.
  4. i.MX 8M Nano EVK board has passed the Arm SR-IR certification.
- Userspace Ethernet DPDK Driver
  1. Supports TSN-QBV on i.MX 95.
  2. Supports OpenSSL-based applications on i.MX 95.
- The following boards are not supported in this release:
  1. i.MX 8QuadXPlus B0 MEK
  2. i.MX 8DXL A1 DDR3L EVK
  3. i.MX 6QuadPlus SABRE-AI
  4. i.MX 6Quad/Dual SABRE-AI
  5. i.MX 6DualLite SABRE-AI  


# Host Setup
To get the Yocto Project expected behavior in a Linux Host Machine, the packages and utilities described
below must be installed. An important consideration is the hard disk space required in the host machine. For
example, when building on a machine running Ubuntu, the minimum hard disk space required is about 50 GB.
It is recommended that at least 120 GB is provided, which is enough to compile all backends together. For
building machine learning components, at least 250 GB is recommended.

The recommended minimum Ubuntu version is 20.04 or later.

### 1. Host packages
```
$ sudo apt install gawk wget git diffstat unzip texinfo gcc build-essential
 chrpath socat cpio python3 python3-pip python3-pexpect xz-utils debianutils
 iputils-ping python3-git python3-jinja2 python3-subunit zstd liblz4-tool file locales libacl1
```

### 2. Build configurations
```
$ DISTRO=<distro name> MACHINE=<machine name> source imx-setup-release.sh -b
 <build dir>

eg:

$ EULA=1 DISTRO=fsl-imx-xwayland MACHINE=imx8mp-lpddr4-evk source imx-setup-release.sh -b Model_AB_Infinity
$ bitbake imx-image-full
```

---

# Debix Model AB — Customizations

This section documents the customizations made on top of the NXP i.MX 8M Plus BSP
for the **Polyhex Debix Model AB** (LPDDR4 4GB, 16GB eMMC) with the **Model A IO
extension board**.

## Machine

A dedicated machine has been created:

- **`imx8mp-debix-model-ab`** — `sources/meta-imx/meta-imx-bsp/conf/machine/imx8mp-debix-model-ab.conf`
  - Inherits `imx8mp-lpddr4-evk` (LPDDR4, DDR firmware, bootloader)
  - Kernel DTB: `freescale/imx8mp-debix-io-board.dtb` (describes the IO extension:
    PCIe, camera imx219, audio es8316, ADC ads1115, HDMI)
  - U-Boot DTB: `imx8mp-evk.dtb` (the one that boots the board)
  - TFA compatibility added in `meta-freescale/dynamic-layers/meta-arm/recipes-bsp/trusted-firmware-a/trusted-firmware-a_%.bbappend`

Set it in `Model_AB_Infinity/conf/local.conf`:
```
MACHINE ??= 'imx8mp-debix-model-ab'
```

## U-Boot (4GB DDR fix)

The board did not boot with the stock 2GB U-Boot. The DEBIX fork provides a 4GB branch:

- `sources/meta-imx/meta-imx-bsp/recipes-bsp/u-boot/u-boot-imx-common_2024.04.inc`
  - `SRCBRANCH = "lf_v2024.04-yocto_L6.12.3-debix_model_ab_4gbddr"`
  - `SRCREV = "666557f2d6dafab3725a6bcc6c744678b4326dc2"` (commit "set to 4GB ddr config")

A U-Boot config fragment forces `fdtfile` to the kernel DTB of the IO board:

- `sources/meta-imx/meta-imx-bsp/recipes-bsp/u-boot/u-boot-imx_%.bbappend`
- `sources/meta-imx/meta-imx-bsp/recipes-bsp/u-boot/files/0001-debix-model-ab-fdtfile.cfg`
  ```
  CONFIG_DEFAULT_FDT_FILE="imx8mp-debix-io-board.dtb"
  ```

## Images

### `imx-image-multimedia`
NXP reference image (multimedia, no QT6/OpenCV/ML). Docker removed
(`DOCKER:mx8-nxp-bsp = ""` in `imx-image-multimedia.bb`).

### `imx-image-minimal` (new)
Minimal console-only image with basic services:
- SSH server: **dropbear**
- Networking: `iproute2`, `ethtool`
- GPIO: `libgpiod-tools`
- I2C: `i2c-tools`
- Debix tools: `debix-custom` (debix-gpio, scripts)

Recipe: `sources/meta-imx/meta-imx-bsp/recipes-polyhex/images/imx-image-minimal.bb`

Notes:
- Excludes `packagegroup-base-extended` (no wifi/bt/3g/nfc)
- Removes `jailhouse` and `packagegroup-fsl-optee-imx` (added by default by meta-imx-bsp)
- **Disables the DEBIX auto-expand script** (`expand_sd_rootfs.sh`) via
  `ROOTFS_POSTPROCESS_COMMAND` — this script corrupts the rootfs partition on
  first boot for small images. The image boots directly at its built size.

## Other fixes

- **pseudo** upgraded to 1.9.11 (`pseudo_git.bb`) to fix the `openat2`/tar
  "Bad address" failure on Ubuntu 24.04 (YOCTO #16316).
- **gstreamer1.0-plugins-bad**: `rsvg` PACKAGECONFIG removed
  (`gstreamer1.0-plugins-bad_%.bbappend`) to avoid pulling the Rust toolchain.
- **tensorflow-lite**: `XNNPACK_ENABLE_KLEIDIAI=off` added to work around a
  KleidiAI SHA256 mismatch.

## Layers

`Model_AB_Infinity/conf/bblayers.conf` has been trimmed. Disabled layers
(commented out): `meta-imx-ml`, `meta-nxp-demo-experience`,
`meta-nxp-connectivity/*`, `meta-gnome`, `meta-qt6`, `meta-security/*`,
`meta-virtualization`.

## Useful commands

```sh
cd /home/chelarica/Documents/yocto/yocto-nxp-debix
source setup-environment Model_AB_Infinity

# Build images
bitbake imx-image-minimal
bitbake imx-image-multimedia

# Rebuild U-Boot / imx-boot only
bitbake -c cleanall u-boot-imx imx-boot
bitbake imx-boot

# Inspect a .wic image
wic ls tmp/deploy/images/imx8mp-debix-model-ab/imx-image-minimal-imx8mp-debix-model-ab.rootfs.wic
wic ls <image>.wic:1        # boot partition (fat32)
wic ls <image>.wic:2        # rootfs (ext4)

# Flash a .wic to an SD card (replace sdX with your device)
sudo dd if=tmp/deploy/images/imx8mp-debix-model-ab/imx-image-minimal-imx8mp-debix-model-ab.rootfs.wic of=/dev/sdX bs=4M conv=fsync status=progress
sudo sync

# Flash only the bootloader (U-Boot) at offset 32KB
sudo dd if=tmp/deploy/images/imx8mp-debix-model-ab/imx-boot-imx8mp-debix-model-ab-sd.bin-flash_evk of=/dev/sdX bs=1k seek=32 conv=fsync
```

Deployed artifacts are in `tmp/deploy/images/imx8mp-debix-model-ab/`:
- `imx-image-minimal-imx8mp-debix-model-ab.rootfs.wic`
- `imx-image-multimedia-imx8mp-debix-model-ab.rootfs.wic`
- `imx-boot-imx8mp-debix-model-ab-sd.bin-flash_evk`
- `Image`, `imx8mp-debix-io-board.dtb`

 
