import prisma from "../prisma";

/**
 * Retrieves the user profile including avatarUrl.
 */
export async function getUserProfile(userId: string) {
  const user = await prisma.user.findUnique({
    where: { id: userId },
    select: {
      id: true,
      email: true,
      name: true,
      garageName: true,
      avatarUrl: true,
    },
  });
  return user;
}

/**
 * Updates user profile (name, garageName, and avatarUrl).
 */
export async function updateUserProfile(
  userId: string,
  name?: string,
  garageName?: string,
  avatarUrl?: string | null
): Promise<{ ok: boolean }> {
  const updates: any = {};
  
  if (name !== undefined && name.trim()) {
    updates.name = name.trim();
  }
  
  if (garageName !== undefined && garageName.trim()) {
    updates.garageName = garageName.trim();
  }

  // avatarUrl can be explicitly null (to clear) or a string (to set)
  if (avatarUrl !== undefined) {
    updates.avatarUrl = avatarUrl || null;
  }

  if (Object.keys(updates).length === 0) {
    return { ok: true };
  }

  await prisma.user.update({
    where: { id: userId },
    data: updates,
  });

  return { ok: true };
}

/**
 * Permanently deletes the user account and all associated data.
 * Prisma cascades deletes to vehicles, serviceLogs, parts, pieces, and
 * serviceLogPieceCrossRefs via onDelete: Cascade relations.
 */
export async function deleteUserAccount(userId: string): Promise<{ ok: boolean }> {
  await prisma.user.delete({
    where: { id: userId },
  });

  return { ok: true };
}
