package hipsterfm



import org.junit.*
import grails.test.mixin.*

@TestFor(TrackController)
@Mock(Track)
class TrackControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/track/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.trackInstanceList.size() == 0
        assert model.trackInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.trackInstance != null
    }

    void testSave() {
        controller.save()

        assert model.trackInstance != null
        assert view == '/track/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/track/show/1'
        assert controller.flash.message != null
        assert Track.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/track/list'

        populateValidParams(params)
        def track = new Track(params)

        assert track.save() != null

        params.id = track.id

        def model = controller.show()

        assert model.trackInstance == track
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/track/list'

        populateValidParams(params)
        def track = new Track(params)

        assert track.save() != null

        params.id = track.id

        def model = controller.edit()

        assert model.trackInstance == track
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/track/list'

        response.reset()

        populateValidParams(params)
        def track = new Track(params)

        assert track.save() != null

        // test invalid parameters in update
        params.id = track.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/track/edit"
        assert model.trackInstance != null

        track.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/track/show/$track.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        track.clearErrors()

        populateValidParams(params)
        params.id = track.id
        params.version = -1
        controller.update()

        assert view == "/track/edit"
        assert model.trackInstance != null
        assert model.trackInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/track/list'

        response.reset()

        populateValidParams(params)
        def track = new Track(params)

        assert track.save() != null
        assert Track.count() == 1

        params.id = track.id

        controller.delete()

        assert Track.count() == 0
        assert Track.get(track.id) == null
        assert response.redirectedUrl == '/track/list'
    }
}
