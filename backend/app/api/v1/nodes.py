from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from datetime import datetime
from app.database import get_db, Node
from app.models.schemas import NodeCreate, NodeOut

router = APIRouter()

@router.get("", response_model=list[NodeOut])
async def list_nodes(db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Node))
    nodes = result.scalars().all()
    return nodes

@router.get("/{node_id}", response_model=NodeOut)
async def get_node(node_id: str, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Node).where(Node.id == node_id))
    node = result.scalar_one_or_none()
    if not node:
        raise HTTPException(status_code=404, detail="Node not found")
    return node

@router.post("", response_model=NodeOut)
async def register_or_update_node(payload: NodeCreate, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Node).where(Node.id == payload.id))
    node = result.scalar_one_or_none()
    if node:
        node.name = payload.name
        node.location = payload.location
        node.status = payload.status
        node.ip_address = payload.ip_address
        node.firmware_version = payload.firmware_version
        node.last_seen = datetime.utcnow()
    else:
        node = Node(
            id=payload.id,
            name=payload.name,
            location=payload.location,
            status=payload.status,
            ip_address=payload.ip_address,
            firmware_version=payload.firmware_version,
            last_seen=datetime.utcnow()
        )
        db.add(node)
    await db.commit()
    await db.refresh(node)
    return node
