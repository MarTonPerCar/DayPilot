#include <glib.h>

#include "my_application.h"

int main(int argc, char** argv) {
  // Native Wayland can't position windows; force XWayland for the flyout.
  g_setenv("GDK_BACKEND", "x11", /*overwrite=*/FALSE);

  g_autoptr(MyApplication) app = my_application_new();
  return g_application_run(G_APPLICATION(app), argc, argv);
}
