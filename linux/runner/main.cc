#include <glib.h>

#include "my_application.h"

int main(int argc, char** argv) {
  // Force XWayland: native Wayland doesn't let clients position their own
  // windows at all, which the flyout's corner-anchoring depends on. Must be
  // set before GTK/GDK touch the display, and only if the user hasn't
  // already chosen a backend themselves.
  g_setenv("GDK_BACKEND", "x11", /*overwrite=*/FALSE);

  g_autoptr(MyApplication) app = my_application_new();
  return g_application_run(G_APPLICATION(app), argc, argv);
}
