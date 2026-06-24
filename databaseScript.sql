USE [BD2024_1]
GO
/****** Object:  Table [dbo].[cab_Factura]    Script Date: 13/05/2025 13:35:07 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[cab_Factura](
	[cab_numFac] [varchar](50) NOT NULL,
	[cab_fechaFac] [date] NULL,
	[cli_id] [varchar](13) NULL,
	[cab_subtotal] [float] NULL,
	[cab_basecero] [float] NULL,
	[cab_iva] [float] NULL,
	[cab_total] [float] NULL,
	[cab_estado] [varchar](1) NULL,
	[usr_id] [int] NULL,
 CONSTRAINT [PK_cab_Factura] PRIMARY KEY CLUSTERED 
(
	[cab_numFac] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Cliente]    Script Date: 13/05/2025 13:35:08 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Cliente](
	[cli_id] [varchar](13) NOT NULL,
	[cli_nombres] [varchar](50) NULL,
	[cli_apellidos] [varchar](50) NULL,
	[cli_telefono] [varchar](13) NULL,
	[cli_correo] [varchar](50) NULL,
	[cli_direccion] [varchar](250) NULL,
	[cli_estado] [varchar](1) NULL,
 CONSTRAINT [PK_Cliente] PRIMARY KEY CLUSTERED 
(
	[cli_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[det_Factura]    Script Date: 13/05/2025 13:35:08 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[det_Factura](
	[det_id] [int] IDENTITY(1,1) NOT NULL,
	[cab_numFac] [varchar](50) NULL,
	[pro_id] [varchar](50) NULL,
	[det_cantidad] [float] NULL,
	[det_iva] [float] NULL,
	[det_pvp] [float] NULL,
	[det_total] [float] NULL,
 CONSTRAINT [PK_det_Factura] PRIMARY KEY CLUSTERED 
(
	[det_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Producto]    Script Date: 13/05/2025 13:35:08 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Producto](
	[pro_id] [varchar](50) NOT NULL,
	[pro_nombre] [varchar](50) NOT NULL,
	[pro_pecioU] [float] NULL,
	[pro_pvp] [float] NULL,
	[pro_strok] [float] NULL,
	[pro_aplicaIva] [int] NULL,
	[pro_estado] [varchar](1) NULL,
 CONSTRAINT [PK_Producto] PRIMARY KEY CLUSTERED 
(
	[pro_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Usuario]    Script Date: 13/05/2025 13:35:08 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Usuario](
	[usr_id] [int] IDENTITY(1,1) NOT NULL,
	[usr_nombreCompleto] [varchar](80) NULL,
	[usr_usuario] [varchar](30) NULL,
	[usr_clave] [varchar](max) NULL,
	[usr_estado] [varchar](1) NULL,
 CONSTRAINT [PK_Usuario] PRIMARY KEY CLUSTERED 
(
	[usr_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
INSERT [dbo].[cab_Factura] ([cab_numFac], [cab_fechaFac], [cli_id], [cab_subtotal], [cab_basecero], [cab_iva], [cab_total], [cab_estado], [usr_id]) VALUES (N'100', CAST(N'2024-05-28' AS Date), N'0987654321', 240, 5, 36, 281, N'A', 1)
INSERT [dbo].[Cliente] ([cli_id], [cli_nombres], [cli_apellidos], [cli_telefono], [cli_correo], [cli_direccion], [cli_estado]) VALUES (N'0987654321', N'PABLO MARIO', N'CARRILLO DIAZ', N'097845', N'pcarrillo@upse.edu.ec', N'Salinas AV10 y 16', N'A')
INSERT [dbo].[Cliente] ([cli_id], [cli_nombres], [cli_apellidos], [cli_telefono], [cli_correo], [cli_direccion], [cli_estado]) VALUES (N'245112518', N'MARIA', N'SALINAS', N'09845', N'msalinas@upse.edu.ec', N'La Libertad av17 y  12', N'A')
SET IDENTITY_INSERT [dbo].[det_Factura] ON 

INSERT [dbo].[det_Factura] ([det_id], [cab_numFac], [pro_id], [det_cantidad], [det_iva], [det_pvp], [det_total]) VALUES (3, N'100', N'100', 1, 36, 200, 200)
INSERT [dbo].[det_Factura] ([det_id], [cab_numFac], [pro_id], [det_cantidad], [det_iva], [det_pvp], [det_total]) VALUES (4, N'100', N'200', 1, 0, 5, 5)
SET IDENTITY_INSERT [dbo].[det_Factura] OFF
INSERT [dbo].[Producto] ([pro_id], [pro_nombre], [pro_pecioU], [pro_pvp], [pro_strok], [pro_aplicaIva], [pro_estado]) VALUES (N'100', N'Televisor LG', 200, 240, 10, 1, N'A')
INSERT [dbo].[Producto] ([pro_id], [pro_nombre], [pro_pecioU], [pro_pvp], [pro_strok], [pro_aplicaIva], [pro_estado]) VALUES (N'200', N'Cuaderno U', 5, 6, 15, 0, N'A')
SET IDENTITY_INSERT [dbo].[Usuario] ON 

INSERT [dbo].[Usuario] ([usr_id], [usr_nombreCompleto], [usr_usuario], [usr_clave], [usr_estado]) VALUES (1, N'JAIME OROZCO', N'jorozco', N'123', N'A')
INSERT [dbo].[Usuario] ([usr_id], [usr_nombreCompleto], [usr_usuario], [usr_clave], [usr_estado]) VALUES (2, N'Karol Torres', N'ktorres', N'321', N'A')
INSERT [dbo].[Usuario] ([usr_id], [usr_nombreCompleto], [usr_usuario], [usr_clave], [usr_estado]) VALUES (3, N'karina villao', N'kvillao', N'123', N'E')
INSERT [dbo].[Usuario] ([usr_id], [usr_nombreCompleto], [usr_usuario], [usr_clave], [usr_estado]) VALUES (4, N'prueba', N'nuevousr', N'122', N'A')
SET IDENTITY_INSERT [dbo].[Usuario] OFF
ALTER TABLE [dbo].[cab_Factura]  WITH CHECK ADD  CONSTRAINT [FK_cab_Factura_Cliente] FOREIGN KEY([cli_id])
REFERENCES [dbo].[Cliente] ([cli_id])
GO
ALTER TABLE [dbo].[cab_Factura] CHECK CONSTRAINT [FK_cab_Factura_Cliente]
GO
ALTER TABLE [dbo].[cab_Factura]  WITH CHECK ADD  CONSTRAINT [FK_cab_Factura_Usuario] FOREIGN KEY([usr_id])
REFERENCES [dbo].[Usuario] ([usr_id])
GO
ALTER TABLE [dbo].[cab_Factura] CHECK CONSTRAINT [FK_cab_Factura_Usuario]
GO
ALTER TABLE [dbo].[det_Factura]  WITH CHECK ADD  CONSTRAINT [FK_det_Factura_cab_Factura] FOREIGN KEY([cab_numFac])
REFERENCES [dbo].[cab_Factura] ([cab_numFac])
GO
ALTER TABLE [dbo].[det_Factura] CHECK CONSTRAINT [FK_det_Factura_cab_Factura]
GO
ALTER TABLE [dbo].[det_Factura]  WITH CHECK ADD  CONSTRAINT [FK_det_Factura_Producto] FOREIGN KEY([pro_id])
REFERENCES [dbo].[Producto] ([pro_id])
GO
ALTER TABLE [dbo].[det_Factura] CHECK CONSTRAINT [FK_det_Factura_Producto]
GO
/****** Object:  StoredProcedure [dbo].[sp_mant_usuario]    Script Date: 13/05/2025 13:35:08 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
create procedure [dbo].[sp_mant_usuario]
(
@id int,
@usu_nombre varchar(80),
@usu_usuario varchar(30),
@usu_clave varchar(50),
@usu_estado varchar(1)
)
as
begin
 if @id=0
 begin
 --inserte
 insert into Usuario (usr_nombreCompleto,usr_usuario,usr_clave,usr_estado)
 values(@usu_nombre,@usu_usuario,@usu_clave,@usu_estado)
 end
 else
 begin
  update usuario set usr_nombreCompleto=@usu_nombre, usr_usuario=@usu_usuario,usr_clave=@usu_clave,usr_estado=@usu_estado
  where usr_id=@id
 end
end
GO
/****** Object:  StoredProcedure [dbo].[sp_selUsuario]    Script Date: 13/05/2025 13:35:08 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE procedure [dbo].[sp_selUsuario]  
(  
@miEstado varchar(1)  
)  
as  
begin  
if @miEstado=''
begin
   select * from Usuario  
end
else
  begin
     select * from Usuario 
    where usr_estado=@miEstado 
  end
end
GO