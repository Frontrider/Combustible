package com.noobanidus.combustible;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class TileEntityWoodenFurnace extends TileEntityFurnace {
  public TileEntityWoodenFurnace() {
  }

  @Override
  public String getName() {
    String name = super.getName();
    if (name.equals("container.furnace")) {
      return "container.wooden_furnace";
    } else {
      return name;
    }
  }

  @Override
  public int getCookTime(ItemStack stack) {
    int original = super.getCookTime(stack);
    int cookTime = CombustibleConfig.cookTime;
    if (CombustibleConfig.useCookMultiplier) {
      cookTime = original * CombustibleConfig.cookMultiplier;
    }
    return cookTime;
  }

  @Override
  public void update() {
    boolean flag = this.isBurning();
    boolean flag1 = false;

    if (this.isBurning()) {
      --this.furnaceBurnTime;
    }

    if (!this.world.isRemote) {
      ItemStack itemstack = this.furnaceItemStacks.get(1);

      if (this.isBurning() || !itemstack.isEmpty() && !(this.furnaceItemStacks.get(0)).isEmpty()) {
        if (!this.isBurning() && this.canSmelt()) {
          this.furnaceBurnTime = getItemBurnTime(itemstack);
          this.currentItemBurnTime = this.furnaceBurnTime;

          if (this.isBurning()) {
            flag1 = true;

            if (!itemstack.isEmpty()) {
              Item item = itemstack.getItem();
              itemstack.shrink(1);

              if (itemstack.isEmpty()) {
                ItemStack item1 = item.getContainerItem(itemstack);
                this.furnaceItemStacks.set(1, item1);
              }
            }
          }
        }

        if (this.isBurning() && this.canSmelt()) {
          ++this.cookTime;

          if (this.cookTime == this.totalCookTime) {
            this.cookTime = 0;
            this.totalCookTime = this.getCookTime(this.furnaceItemStacks.get(0));
            this.smeltItem();
            flag1 = true;
          }
        } else {
          this.cookTime = 0;
        }
      } else if (!this.isBurning() && this.cookTime > 0) {
        this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);
      }

      if (flag != this.isBurning()) {
        flag1 = true;
        BlockWoodenFurnace.setState(this.isBurning(), this.world, this.pos);
      }
    }

    if (flag1) {
      this.markDirty();
    }

    if (!world.isRemote && this.isBurning() && CombustibleConfig.combustibleFurnaces) {
      if (world.rand.nextInt(CombustibleConfig.combustionChance) == 0) {
        if (CombustibleConfig.increaseFirePlacementRange) {
          for (BlockPos.MutableBlockPos pos : BlockPos.getAllInBoxMutable(getPos().add(-2, -2, 0), getPos().add(0, 2, 2))) {
            if (world.isAirBlock(pos)) {
              world.setBlockState(pos, Blocks.FIRE.getDefaultState());
              break;
            }
          }
        } else {
          for (EnumFacing facing : EnumFacing.VALUES) {
            if (world.isAirBlock(pos.offset(facing))) {
              world.setBlockState(pos.offset(facing), Blocks.FIRE.getDefaultState());
              break;
            }
          }
        }
      }
    }
  }
}
