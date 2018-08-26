define ([], function () {

    $_DO.update_living_room_popup = function (e) {

        var form = w2ui ['living_room_new_form']

        var v = form.values ()
        
        if ($_REQUEST.type == 'house') {

            var data = $('body').data ('data')

            var house = data.item

            if (house.is_condo) {

                if (!v.uuid_premise) die ('uuid_premise', 'Укажите, пожалуйста, квартиру')

            }
            else if (!v.uuid_block) {

                if (data.vc_blocks.items.length) {
                    die ('uuid_block', 'Укажите, пожалуйста, блок')
                }
                else {
                    v.uuid_house = house.uuid
                }

            }

        }

        if (v.roomnumber == null || v.roomnumber == '') die ('roomnumber', 'Укажите, пожалуйста, номер комнаты')
        if (!/[0-9А-ЯЁа-яёA-Za-z]/.test (v.roomnumber)) die ('roomnumber', 'Некорректный номер комнаты')

        if (parseFloat (v.square || '0') < 0.01) die ('square', 'Необходимо указать размер плошади')

        if ($_REQUEST.type == 'premise_residental') v.uuid_premise = $_REQUEST.id

        var enums = {
            f_20056: 1,
        }

        for (i in enums) {v [i] = v [i] ? [v [i]] : []}

        query ({type: 'living_rooms', action: 'create', id: undefined}, {data: v}, reload_page)

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.get ('record')
        sessionStorage.removeItem ('record')
        
        if (data.vc_blocks && data.vc_blocks.items.length == 1) data.record.uuid_block = data.vc_blocks.items [0]
        
        if ($_REQUEST.type == 'premise_residental') {
        
            var dd = {vc_premises: [{
                id: data.record.uuid_premise = data.item.uuid, 
                label: data.item.premisesnum
            }]}
        
            add_vocabularies (dd, dd)
            
            for (i in dd) data [i] = dd [i]

        }

        done (data)

    }

})