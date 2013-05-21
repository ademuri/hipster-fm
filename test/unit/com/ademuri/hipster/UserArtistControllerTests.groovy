package com.ademuri.hipster



import org.junit.*

import com.ademuri.hipster.UserArtist;
import com.ademuri.hipster.UserArtistController;

import grails.test.mixin.*

@TestFor(UserArtistController)
@Mock(UserArtist)
class UserArtistControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/artist/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.artistInstanceList.size() == 0
        assert model.artistInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.artistInstance != null
    }

    void testSave() {
        controller.save()

        assert model.artistInstance != null
        assert view == '/artist/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/artist/show/1'
        assert controller.flash.message != null
        assert UserArtist.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/artist/list'

        populateValidParams(params)
        def artist = new UserArtist(params)

        assert artist.save() != null

        params.id = artist.id

        def model = controller.show()

        assert model.artistInstance == artist
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/artist/list'

        populateValidParams(params)
        def artist = new UserArtist(params)

        assert artist.save() != null

        params.id = artist.id

        def model = controller.edit()

        assert model.artistInstance == artist
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/artist/list'

        response.reset()

        populateValidParams(params)
        def artist = new UserArtist(params)

        assert artist.save() != null

        // test invalid parameters in update
        params.id = artist.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/artist/edit"
        assert model.artistInstance != null

        artist.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/artist/show/$artist.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        artist.clearErrors()

        populateValidParams(params)
        params.id = artist.id
        params.version = -1
        controller.update()

        assert view == "/artist/edit"
        assert model.artistInstance != null
        assert model.artistInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/artist/list'

        response.reset()

        populateValidParams(params)
        def artist = new UserArtist(params)

        assert artist.save() != null
        assert UserArtist.count() == 1

        params.id = artist.id

        controller.delete()

        assert UserArtist.count() == 0
        assert UserArtist.get(artist.id) == null
        assert response.redirectedUrl == '/artist/list'
    }
}
