package mips_cpu_2nd

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

trait mips_macros {
    // inst & func def
val OP_ADDI  = "b001000".U(6.W)
val OP_ADDIU = "b001001".U(6.W)
val OP_SLTI  = "b001010".U(6.W)
val OP_SLTIU = "b001011".U(6.W)
val OP_ANDI ="b001100".U(6.W)
val OP_LUI = "b001111".U(6.W)
val OP_ORI = "b001101".U(6.W)
val OP_XORI = "b001110".U(6.W)
val OP_BEQ = "b000100".U(6.W)
val OP_BNE = "b000101".U(6.W)
val OP_BGTZ = "b000111".U(6.W)
val OP_BLEZ = "b000110".U(6.W)
val OP_J = "b000010".U(6.W)
val OP_JAL = "b000011".U(6.W)
val OP_LB = "b100000".U(6.W)
val OP_LBU = "b100100".U(6.W)
val OP_LH = "b100001".U(6.W)
val OP_LHU = "b100101".U(6.W)
val OP_LW = "b100011".U(6.W)
val OP_SB = "b101000".U(6.W)
val OP_SH = "b101001".U(6.W)
val OP_SW = "b101011".U(6.W)
val OP_LWL = "b100010".U(6.W)
val OP_LWR = "b100110".U(6.W)
val OP_SWL = "b101010".U(6.W)
val OP_SWR = "b101110".U(6.W)
val OP_PREF = "b110011".U(6.W) //prediction

val OP_CACHE = "b101111".U(6.W) //cache指令，后面应该是只实现了其中几个，后续仔细讨论
val OP_SPECIAL = "b000000".U(6.W)
val OP_REGIMM = "b000001".U(6.W)
val OP_PRIVILEGE = "b010000".U(6.W) //cop0
val OP_SPECIAL2 = "b011100".U(6.W)

//包含special
val FUNC_ADD = "b100000".U(6.W)
val FUNC_ADDU = "b100001".U(6.W)
val FUNC_SUB = "b100010".U(6.W)
val FUNC_SUBU = "b100011".U(6.W)
val FUNC_SLT = "b101010".U(6.W)
val FUNC_SLTU = "b101011".U(6.W)
val FUNC_DIV = "b011010".U(6.W)
val FUNC_DIVU = "b011011".U(6.W)
val FUNC_MULT = "b011000".U(6.W)
val FUNC_MULTU = "b011001".U(6.W)
val FUNC_AND = "b100100".U(6.W)
val FUNC_NOR = "b100111".U(6.W)
val FUNC_OR = "b100101".U(6.W)
val FUNC_XOR = "b100110".U(6.W)
val FUNC_SLL = "b000000".U(6.W)
val FUNC_SLLV = "b000100".U(6.W)
val FUNC_SRA = "b000011".U(6.W)
val FUNC_SRAV = "b000111".U(6.W)
val FUNC_SRL = "b000010".U(6.W)
val FUNC_SRLV = "b000110".U(6.W)
val FUNC_JR = "b001000".U(6.W)
val FUNC_JALR = "b001001".U(6.W)
val FUNC_MFHI = "b010000".U(6.W)
val FUNC_MFLO = "b010010".U(6.W)
val FUNC_MTHI = "b010001".U(6.W)
val FUNC_MTLO = "b010011".U(6.W)

val FUNC_MOVN = "b001011".U(6.W) //不等于零复制 rt =/= 0 ,则rs -> rd
val FUNC_MOVZ = "b001010".U(6.W) //等于零复制
//自陷指令
val FUNC_BREAK = "b001101".U(6.W)
val FUNC_SYSCALL = "b001100".U(6.W)
val FUNC_TEQ = "b110100".U(6.W)
val FUNC_TNE = "b110110".U(6.W)
val FUNC_TGE = "b110000".U(6.W) //大于等于就trap    as 有符号整数
val FUNC_TGEU = "b110001".U(6.W) //大于等于就trap    as 无符号整数
val FUNC_TLT = "b110010".U(6.W) //小与就trap    as 有符号整数
val FUNC_TLTU = "b110011".U(6.W) //小于就trap    as 无符号整数


//包含special2
val FUNC_CLO  = "b100001".U(6.W) //
val FUNC_CLZ  = "b100000".U(6.W) //count leading zeros
val FUNC_MUL  = "b000010".U(6.W) // 乘法，结果放到普通寄存器里面去
val FUNC_MADD = "b000000".U(6.W) // 加乘，这个指令得拆，没办法，谁叫他这么复杂
val FUNC_MADDU = "b000001".U(6.W) // 无符号加乘，这个指令得拆，没办法，谁叫他这么复杂
val FUNC_MSUB = "b000100".U(6.W)//没有任何溢出例外
val FUNC_MSUBU = "b000100".U(6.W) //没有任何溢出例外




//REGIMM 以下
val RT_BGEZ = "b00001".U(5.W)
val RT_BGEZAL = "b10001".U(5.W)
val RT_BLTZ = "b00000".U(5.W)
val RT_BLTZAL = "b10000".U(5.W)
val RT_TEQI = "b01100".U(5.W) //如果相等则trap
val RT_TNEI = "b01110".U(5.W)
val RT_TGEI = "b01000".U(5.W) //大于等于就trap , as 有符号整数
val RT_TGEIU = "b01001".U(5.W) //大于等于就trap , as 无符号整数
val RT_TLTI = "b01010".U(5.W) //小于就trap , as 有符号整数
val RT_TLTIU = "b01011".U(5.W) //小于就trap , as 无符号整数

//特权指令🏇
val CO_SET = 1.U(1.W)
val CO_RESET = 0.U(1.W)
// val RS_ERET = "b10000".U(5.W)
//co_reset
val COP_MFC0 = "b0000".U(5.W)
val COP_MTC0 = "b0100".U(5.W)
//co_set
val FUNC_TLBP =  "b001000".U(6.W)
val FUNC_TLBR =  "b000001".U(6.W)
val FUNC_TLBWI = "b000010".U(6.W)
val FUNC_TLBWR = "b000110".U(6.W)
val FUNC_ERET = "b011000".U(6.W)
val FUNC_WAIT = "b100000".U(6.W)

// val FULL_ERET =  "b01000010_00000000_00000000_00011000".U(32.W)



// // inst_type id def    "b00_10".U -> Cat(0.U(16.W),data(15,0)),
//             "b00_11".U -> data,
//             "b01_01".U -> Cat(0.U(16.W),data(7,0),0.U(8.W)),
//             // "b01_10".U -> Cat(0.U(8.W),data(15,0),0.U(8.W)),//SH和LH只能读高两位或者低两位
//             "b10_01".U -> Cat(0.U(8.W),data(7,0),0.U(16.W)),
//             "b10_10".U -> Cat(data(15,0),0.U(16.W)),
//             "b11_01".U -> Cat(data(7,0),0.U(24.W))
val ID_NULL= 0
val ID_ADD =1
val ID_ADDI =2
val ID_ADDU =3
val ID_ADDIU= 4
val ID_SUB =5
val ID_SUBU= 6
val ID_SLT =7
val ID_SLTI= 8
val ID_SLTU= 9
val ID_SLTIU= 10
val ID_DIV= 11
val ID_DIVU =12
val ID_MULT =13
val ID_MULTU =14
val ID_AND =15
val ID_ANDI =16
val ID_LUI= 17
val ID_NOR= 18
val ID_OR =19
val ID_ORI= 20
val ID_XOR =21
val ID_XORI= 22
val ID_SLL= 23
val ID_SLLV =24
val ID_SRA= 25
val ID_SRAV= 26
val ID_SRL =27
val ID_SRLV =28
val ID_BEQ =29
val ID_BNE =30
val ID_BGEZ= 31
val ID_BGEZAL= 32
val ID_BGTZ =33
val ID_BLEZ= 34
val ID_BLTZ= 35
val ID_BLTZAL= 36
val ID_J= 37
val ID_JAL =38
val ID_JR =39
val ID_JALR =40
val ID_MFHI =41
val ID_MFLO= 42
val ID_MTHI =43
val ID_MTLO= 44
val ID_BREAK= 45
val ID_SYSCALL= 46
val ID_LB =47
val ID_LBU =48
val ID_LH =49
val ID_LHU= 50
val ID_LW= 51
val ID_SB =52
val ID_SH =53
val ID_SW =54
val ID_ERET =55
val ID_MFC0= 56
val ID_MTC0= 57
val ID_NOP= 58
val ID_LWL=59
val ID_LWR=60
val ID_SWL=61
val ID_SWR=62

val ID_MUL  = 63
val ID_CLO  = 64
val ID_CLZ  = 65
val ID_MADD = 66
val ID_MADDU= 67
val ID_MSUB = 68
val ID_MSUBU= 69
val ID_TEQ  = 70
val ID_TEQI = 71
val ID_TNE  = 72 
val ID_TNEI = 73
val ID_TGE  = 74
val ID_TGEI = 75
val ID_TGEU = 76
val ID_TGEIU= 77
val ID_TLT  = 78
val ID_TLTI = 79
val ID_TLTU = 80
val ID_TLTIU = 81
val ID_CACHE = 82
val ID_TLBP = 83
val ID_TLBR = 84
val ID_TLBWI = 85
val ID_TLBWR = 86
val ID_WAIT = 87
val ID_MOVN = 88
val ID_MOVZ = 89
val ID_SYNC = 90
val ID_PREF = 91


// alu cmd def
val ALU_NULL  = 0
val ALU_ADD   = 1
val ALU_ADDE  = 2
val ALU_ADDU  = 3
val ALU_AND   = 4
val ALU_DIV   = 5
val ALU_DIVU  = 6
val ALU_LUI   = 7
val ALU_MULT  = 8
val ALU_MULTU = 9
val ALU_NOR   = 10
val ALU_OR    = 11
val ALU_SLL   = 12
val ALU_SLT   = 13
val ALU_SLTU  = 14
val ALU_SRA   = 15
val ALU_SRL   = 16
val ALU_SUB   = 17
val ALU_SUBE  = 18
val ALU_SUBU  = 19
val ALU_XOR   = 20
//加入到alu中
val ALU_MOVN  = 21
val ALU_MOVZ  = 22
val ALU_CLO = 28
val ALU_CLZ = 29

val ALU_MUL   = 23
//重命名的时候得分成四份
val ALU_MADD  = 24
val ALU_MADDU = 25
val ALU_MSUB  = 26
val ALU_MSUBU = 27

val opcode_alu = 0.U
val opcode_muldiv = 1.U
val opcode_mem = 2.U
val opcode_branch = 3.U
val opcode_privilege = 4.U
val opcode_self_in = 5.U
val opcode_nop = 6.U





// cu control signals def
// MemRL(2)  BranchD_Flag  RegWriteD(1)	RegDstD(2)	ALUSrcD(2)	ImmUnsigned(1) BranchD(6)	JumpD(1)	JRD(1)	LinkD(1)	
// HiLoWriteD(2)  HiLoToRegD(2)	CP0WriteD(1) CP0ToRegD(1) MemWriteD(1)	MemToRegD(1) LoadUnsignedD(1)	MemWidthD(2)
val CTRL_NULL  =  "b00_0_0_00_00_0_000000_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_ITYPE =  "b00_0_1_00_01_0_000000_0_0_0_00_00_0_0_0_0_0_00".U//JR用于表示是不是JR或者JALR指令，该指令储存在寄存器中
val CTRL_ITYPEU=  "b00_0_1_00_01_1_000000_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_RTYPE =  "b00_0_1_01_00_0_000000_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_RTYPES = "b00_0_1_01_10_0_000000_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_LB  =    "b00_0_1_00_01_0_000000_0_0_0_00_00_0_0_0_1_0_01".U
val CTRL_LBU =    "b00_0_1_00_01_0_000000_0_0_0_00_00_0_0_0_1_1_01".U
val CTRL_LH  =    "b00_0_1_00_01_0_000000_0_0_0_00_00_0_0_0_1_0_10".U
val CTRL_LHU=     "b00_0_1_00_01_0_000000_0_0_0_00_00_0_0_0_1_1_10".U
val CTRL_LW =     "b00_0_1_00_01_0_000000_0_0_0_00_00_0_0_0_1_0_11".U
val CTRL_LWL =    "b10_0_1_00_01_0_000000_0_0_0_00_00_0_0_0_1_0_11".U
val CTRL_LWR =    "b01_0_1_00_01_0_000000_0_0_0_00_00_0_0_0_1_0_11".U
val CTRL_SB =     "b00_0_0_00_01_0_000000_0_0_0_00_00_0_0_1_0_0_01".U
val CTRL_SH =     "b00_0_0_00_01_0_000000_0_0_0_00_00_0_0_1_0_0_10".U
val CTRL_SW =     "b00_0_0_00_01_0_000000_0_0_0_00_00_0_0_1_0_0_11".U
val CTRL_SWL =    "b10_0_0_00_01_0_000000_0_0_0_00_00_0_0_1_0_0_11".U
val CTRL_SWR =    "b01_0_0_00_01_0_000000_0_0_0_00_00_0_0_1_0_0_11".U
val CTRL_BEQ =    "b00_1_0_00_00_0_000001_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_BNE =    "b00_1_0_00_00_0_000010_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_BGEZ =   "b00_1_0_00_00_0_000100_0_0_0_00_00_0_0_0_0_0_00".U//branch 部分仅仅用来代表是大于跳转还是小于跳转
val CTRL_BGEZAL = "b00_1_1_10_00_0_000100_0_0_1_00_00_0_0_0_0_0_00".U
val CTRL_BGTZ =   "b00_1_0_00_00_0_001000_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_BLEZ =   "b00_1_0_00_00_0_010000_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_BLTZ =   "b00_1_0_00_00_0_100000_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_BLTZAL = "b00_1_1_10_00_0_100000_0_0_1_00_00_0_0_0_0_0_00".U
val CTRL_J  =     "b00_0_0_00_00_0_000000_1_0_0_00_00_0_0_0_0_0_00".U
val CTRL_JAL =    "b00_0_1_10_00_0_000000_1_0_1_00_00_0_0_0_0_0_00".U
val CTRL_JR  =    "b00_0_0_00_00_0_000000_1_1_0_00_00_0_0_0_0_0_00".U
val CTRL_JALR =   "b00_0_1_01_00_0_000000_1_1_1_00_00_0_0_0_0_0_00".U
val CTRL_DIV =    "b00_0_0_00_00_0_000000_0_0_0_11_00_0_0_0_0_0_00".U
val CTRL_DIVU =   "b00_0_0_00_00_0_000000_0_0_0_11_00_0_0_0_0_0_00".U

val CTRL_MUL  =   "b00_0_1_01_00_0_000000_0_0_0_00_00_0_0_0_0_0_00".U //这玩意最好一起扔到alu里面去，方便renaming
                                                                                //哈是renameing的时候把hilo和cpo均当作正常的寄存器来进行操作就行了
val CTRL_MULT =   "b00_0_0_00_00_0_000000_0_0_0_11_00_0_0_0_0_0_00".U
val CTRL_MULTU =  "b00_0_0_00_00_0_000000_0_0_0_11_00_0_0_0_0_0_00".U

val CTRL_MFHI =   "b00_0_1_01_00_0_000000_0_0_0_00_10_0_0_0_0_0_00".U
val CTRL_MFLO  =  "b00_0_1_01_00_0_000000_0_0_0_00_01_0_0_0_0_0_00".U
val CTRL_MTHI =   "b00_0_0_00_00_0_000000_0_0_0_10_00_0_0_0_0_0_00".U
val CTRL_MTLO =   "b00_0_0_00_00_0_000000_0_0_0_01_00_0_0_0_0_0_00".U
val CTRL_BREAK =  "b00_0_0_00_00_0_000000_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_SYSCALL ="b00_0_0_00_00_0_000000_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_ERET =   "b00_0_0_00_00_0_000000_0_0_0_00_00_0_0_0_0_0_00".U
val CTRL_MFC0 =   "b00_0_1_00_00_0_000000_0_0_0_00_00_0_1_0_0_0_00".U
val CTRL_MTC0 =   "b00_0_0_00_00_0_000000_0_0_0_00_00_1_0_0_0_0_00".U


// // cp0 address & select
val CP0_ADDR_SEL_INDEX      = "b00000_0".U 
val CP0_ADDR_SEL_RANDOM     = "b00001_0".U 
val CP0_ADDR_SEL_ENTRYLO0   = "b00010_0".U 
val CP0_ADDR_SEL_ENTRYLO1   = "b00011_0".U 
val CP0_ADDR_SEL_PAGEMASK   = "b00101_0".U 
val CP0_ADDR_SEL_BADVADDR   = "b01000_0".U 
val CP0_ADDR_SEL_COUNT      = "b01001_0".U 
val CP0_ADDR_SEL_ENTRYHI    = "b01010_0".U 
val CP0_ADDR_SEL_COMPARE    = "b01011_0".U 
val CP0_ADDR_SEL_STATUS     = "b01100_0".U 
val CP0_ADDR_SEL_CAUSE      = "b01101_0".U 
val CP0_ADDR_SEL_EPC        = "b01110_0".U 
val CP0_ADDR_SEL_PRID       = "b01111_0".U 
val CP0_ADDR_SEL_CONFIG0    = "b10000_0".U 
val CP0_ADDR_SEL_CONFIG1    = "b00000_1".U 


// // exception
val EXCEP_INT       = 0x0  .U     // interrupt
val EXCEP_AdELD     = 0x4  .U     // lw addr error
val EXCEP_AdELI     = 0x14 .U     // pc fetch error
val EXCEP_AdES      = 0x5  .U     // sw addr 
val EXCEP_Sys       = 0x8  .U     // syscall
val EXCEP_Bp        = 0x9  .U     // break point
val EXCEP_RI        = 0xa  .U     // reserved instr
val EXCEP_Ov        = 0xc  .U     // overflow      
val EXCEP_Tr        = 0xd  .U     // trap
val EXCEP_ERET      = 0x1f .U     // eret treated as exception


// // exception mask
val EXCEP_MASK_INT      ="b00000000_00000000_00000000_00000001".U
val EXCEP_MASK_AdELD    ="b00000000_00000000_00000000_00010000".U
val EXCEP_MASK_AdELI    ="b00000000_00010000_00000000_00000000".U
val EXCEP_MASK_AdES     ="b00000000_00000000_00000000_00100000".U
val EXCEP_MASK_Sys      ="b00000000_00000000_00000001_00000000".U
val EXCEP_MASK_Bp       ="b00000000_00000000_00000010_00000000".U
val EXCEP_MASK_RI       ="b00000000_00000000_00000100_00000000".U
val EXCEP_MASK_Ov       ="b00000000_00000000_00010000_00000000".U
val EXCEP_MASK_Tr       ="b00000000_00000000_00100000_00000000".U
val EXCEP_MASK_ERET     ="b10000000_00000000_00000000_00000000".U


// // exception code
val EXCEP_CODE_INT      = 0x0 .U     // interrupt
val EXCEP_CODE_AdEL     = 0x4 .U     // pc fetch or lw addr error
val EXCEP_CODE_AdES     = 0x5 .U     // sw addr 
val EXCEP_CODE_Sys      = 0x8 .U     // syscall
val EXCEP_CODE_Bp       = 0x9 .U     // break point
val EXCEP_CODE_RI       = 0xa .U     // reserved instr
val EXCEP_CODE_Ov       = 0xc .U     // overflow      
val EXCEP_CODE_Tr       = 0xd .U     // trap
val EXCEP_CODE_ERET     = 0x1f.U     // eret treated as exception
val EXCEP_CODE_TLBL     = 0x2 .U     // tlbl, refill bfc00200, invalid bfc00380
val EXCEP_CODE_TLBS     = 0x3 .U     // tlbs, refill bfc00200, invalid bfc00380
val EXCEP_CODE_MOD      = 0x1 .U     // modified

//branch prediction state machine code 
val Strongly_Not_Taken = "b00".U
val Strongly_Taken = "b11".U
val Weakly_Not_Taken = "b01".U
val Weakly_Taken = "b10".U

def sign_extend(value:UInt,length:Int):UInt = 
    Cat(Cat(Seq.fill(32-length)(value(length-1))),value(length-1,0))

def unsign_extend(value:UInt,length:Int):UInt = 
    Cat(0.U(32-length),value(length-1,0))
def Mux2_4(sel:UInt,ch0:UInt,ch1:UInt,ch2:UInt,ch3:UInt):UInt = MuxLookup(sel,0.U,Seq(
    0.U -> ch0,1.U -> ch1,2.U -> ch2,3.U -> ch3))

def get_wstrb(sram_size:UInt,sram_addr:UInt) = {
    MuxLookup(Cat(sram_size,sram_addr),Mux(sram_size === "b10".U,"b1111".U,0.U),Seq(
        "b0000".U -> "b0001".U,
        "b0001".U -> "b0010".U,
        "b0010".U -> "b0100".U,
        "b0011".U -> "b1000".U,
        "b0100".U -> "b0011".U,
        "b0110".U -> "b1100".U
    ))
}
//输入最好为4的整数倍
def Hash(num:UInt) : UInt  = {
  val length = num.getWidth 
  val num_array = Wire(Vec((length/4),UInt(1.W)))
  for(i <- 0 to (length/4)-1) {
    num_array(i) :=  num(((i+1)*4 - 1),i*4).xorR
  }
  num_array.asUInt

}
def  branch_prediction_state_machine_code_decoder(code:UInt) :Bool  = {
    MuxLookup(code,0.asUInt.asBool,Seq(
        Strongly_Taken -> 1.U.asBool,
        Strongly_Not_Taken -> 0.U.asBool,
        Weakly_Not_Taken -> 0.U.asBool,
        Weakly_Taken  -> 1.U.asBool))
} 
}

    // ins_id := MuxLookup(OpD,ID_NULL.U,Seq(
    //     ( OP_ADDI) -> (ID_ADDI).U,
    //     ( OP_ANDI) -> (ID_ANDI).U,
    //     ( OP_ADDIU) -> (ID_ADDIU).U,
    //     ( OP_SLTI) -> (ID_SLTI).U,
    //     ( OP_SLTIU) -> (ID_SLTIU).U,
    //     ( OP_LUI) -> (ID_LUI).U,
    //     ( OP_ORI) -> (ID_ORI).U,
    //     ( OP_XORI) -> (ID_XORI).U,
    //     ( OP_BEQ) -> (ID_BEQ).U,
    //     ( OP_BNE ) -> (ID_BNE.U ),
    //     ( OP_BGTZ) -> (ID_BGTZ.U),
    //     ( OP_BLEZ) -> (ID_BLEZ).U,
    //     ( OP_J   ) -> (ID_J.U ),
    //     ( OP_JAL) -> (ID_JAL.U),
    //     ( OP_LB) -> (ID_LB.U),
    //     ( OP_LBU) -> (ID_LBU.U),
    //     ( OP_LH) -> (ID_LH.U),
    //     ( OP_LHU ) -> (ID_LHU.U ),
    //     ( OP_LW ) ->(ID_LW.U),
    //     ( OP_SB) -> (ID_SB.U),
    //     ( OP_SH) -> (ID_SH.U),
    //     ( OP_SW) -> (ID_SW.U),
    //     (OP_LWL ) -> (ID_LWL).U,
    //     (OP_LWR ) -> (ID_LWR).U,
    //     (OP_SWL ) -> (ID_SWL).U,
    //     (OP_SWR ) -> (ID_SWR).U,
    //     OP_CACHE  -> (ID_CACHE).U, //cache指令，后面应该是只实现了其中几个，后续仔细讨论
    //     OP_PREF   -> (ID_PREF).U,
    //     ( OP_SPECIAL) -> MuxLookup(FunctD,ID_NULL.U,Seq( // 在op相同情况下，根据funct来判断是哪一条指令 这个得写特判
    //         ( FUNC_SUB) ->   (ID_SUB).U,
    //         ( FUNC_AND ) ->   (ID_AND ).U,
    //         ( FUNC_OR) ->   (ID_OR).U,
    //         ( FUNC_SLT) ->   (ID_SLT).U,
    //         ( FUNC_SLL) ->   (ID_SLL).U,
    //         ( FUNC_SLTU ) ->   (ID_SLTU ).U,
    //         ( FUNC_XOR) ->   (ID_XOR).U,
    //         ( FUNC_ADD) ->   (ID_ADD).U,
    //         ( FUNC_ADDU ) ->   (ID_ADDU ).U,
    //         ( FUNC_SUBU ) ->   (ID_SUBU ).U,

    //         ( FUNC_DIV) ->   (ID_DIV).U,
    //         ( FUNC_DIVU) ->   (ID_DIVU).U,
    //         ( FUNC_MULT) ->   (ID_MULT).U,
    //         ( FUNC_MULTU) ->   (ID_MULTU).U,

    //         ( FUNC_NOR) ->   (ID_NOR).U,
    //         ( FUNC_SLLV ) ->   (ID_SLLV ).U,
    //         ( FUNC_SRA) ->   (ID_SRA).U,
    //         ( FUNC_SRAV) ->   (ID_SRAV).U,
    //         ( FUNC_SRL) ->   (ID_SRL).U,
    //         ( FUNC_SRLV) ->   (ID_SRLV ).U,

    //         ( FUNC_JR)   ->   (ID_JR).U,
    //         ( FUNC_JALR) ->   (ID_JALR.U),
            
    //         ( FUNC_MFHI ) ->   (ID_MFHI.U ),
    //         ( FUNC_MFLO) ->   (ID_MFLO.U ),
    //         ( FUNC_MTHI) ->   (ID_MTHI.U),
    //         ( FUNC_MTLO) ->   (ID_MTLO.U),
    //         ( FUNC_BREAK ) ->   (ID_BREAK.U ),
    //         ( FUNC_SYSCALL )->   (ID_SYSCALL.U ),
    //           FUNC_MOVN     -> (ID_MOVN.U),
    //           FUNC_MOVZ     -> ID_MOVZ.U,
    //           FUNC_TEQ      -> (ID_TEQ.U),
    //           FUNC_TNE      -> (ID_TNE.U),
    //           FUNC_TGEU     -> ID_TGEU.U,
    //           FUNC_TGE      -> ID_TGE.U,
    //           FUNC_TLT      -> ID_TLT.U,
    //           FUNC_TLTU     -> ID_TLTU.U)),
    //     OP_SPECIAL2 -> MuxLookup(FunctD,ID_NULL.U,Seq(
    //         FUNC_CLO -> ID_CLO.U,
    //         FUNC_CLZ    -> ID_CLZ.U,
    //         FUNC_MUL    -> ID_MUL.U,
    //         FUNC_MADD   -> ID_MADD.U,
    //         FUNC_MADDU -> ID_MADDU.U,
    //         FUNC_MSUB -> ID_MSUB.U,
    //         FUNC_MSUBU -> ID_MSUBU.U
    //     )),
        
    //     OP_REGIMM -> MuxLookup(RtD,ID_NULL.U,Seq( //后面这里可以改,在id时就开始算分支
    //         RT_BGEZ   -> (ID_BGEZ.U ),
    //         RT_BGEZAL   -> (ID_BGEZAL.U ),
    //         RT_BLTZ   -> (ID_BLTZ.U ),
    //         RT_BLTZAL   -> (ID_BLTZAL.U ),
    //         RT_TEQI -> (ID_TEQI.U ),
    //         RT_TNEI     -> (ID_TNEI.U ),
    //         RT_TGEI     -> (ID_TGEI.U ),
    //         RT_TGEIU     -> (ID_TGEIU.U ),
    //         RT_TLTI     -> (ID_TLTI.U),
    //         RT_TLTIU    -> (ID_TLTIU.U )
    //     )),
    //     ( OP_PRIVILEGE) -> MuxLookup(coD,ID_NULL.U,Seq(
    //         CO_SET -> MuxLookup(FunctD,ID_NULL.U,Seq(
    //                 FUNC_TLBP -> ID_TLBP.U,
    //                 FUNC_TLBR -> ID_TLBR.U,
    //                 FUNC_TLBWI -> ID_TLBWI.U,
    //                 FUNC_TLBWR  -> ID_TLBWR.U,
    //                 FUNC_ERET -> ID_ERET.U,
    //                 FUNC_WAIT -> ID_WAIT.U//暂时不知道是啥，还不想写
    //         )),
    //         CO_RESET -> MuxLookup(coD_res,ID_NULL.U,Seq(
    //                 COP_MFC0 -> ID_MFC0.U,
    //                 COP_MTC0 -> ID_MTC0.U
    //         ))

    //     ))
    // //     // MuxCase(ID_NULL.U,Seq( //后面这里可以改,在id时就开始算分支
    // //     //     (RsD === RS_ERET )  -> (ID_ERET.U ),
    // //     //     (RsD === RS_MFC0 )  -> (ID_MFC0.U ),
    // //     //     (RsD === RS_MTC0 )  -> (ID_MTC0.U )
    // //     // ))

    // ))