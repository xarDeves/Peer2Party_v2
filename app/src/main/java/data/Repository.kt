package data

import androidx.lifecycle.LiveData

//TODO possibly deprecate. LiveData should live in MainViewModel
class Repository(dao: DbDao) {

    val allMessages: LiveData<List<Message>> = dao.getAllMessages()

    /*suspend fun insertEntity(entity: EntityDataClass) {
        dao.insert(entity)
    }

    suspend fun deleteEntity(position: Int) {
        dao.delete(position)
    }

    suspend fun updateChecked(isClicked: Boolean, position: Int) {
        dao.updateChecked(isClicked, position)
    }*/

}