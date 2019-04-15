define ([], function () {

    var grid_name = 'citizen_compensation_category_legal_acts_grid'

    $_DO.create_citizen_compensation_category_legal_acts = function (e) {

        var saved = {
            uuid: $_REQUEST.id
        }

        function done() {

            var tia = {type: 'citizen_compensation_category_legal_acts', action: 'create', id: null}

            query (tia, {data: saved}, function () {

                use.block('citizen_compensation_category_legal_acts')

            })
        }

        $('body').data('legal_acts_popup.callback', function (r) {

            if (!r)
                return

            saved.uuid_legal_act = r.uuid

            done()

        })

        $_SESSION.set('legal_acts_popup.post_data', {search: [
            ], searchLogic: "AND"})

        use.block('legal_acts_popup')
    }

    $_DO.delete_citizen_compensation_category_legal_acts = function (e) {

        var grid = w2ui[grid_name]

        query({

            type: 'citizen_compensation_category_legal_acts',
            id: grid.getSelection() [0],
            action: 'delete',

        }, {data:{uuid_tf: $_REQUEST.id}}, function(){
            use.block('citizen_compensation_category_legal_acts')
        })

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone($('body').data('data'))

        query ({type: 'legal_acts', id: null, part: 'vocs'}, {}, function (d) {

            add_vocabularies (d, d)

            for (k in d) {
                if (k == 'item') continue
                data [k] = d [k]
            }

            $('body').data('data', data)

            data._can = {
                create: $_USER.has_nsi_20(7, 10)
            }

            done (data)
        })
    }

})