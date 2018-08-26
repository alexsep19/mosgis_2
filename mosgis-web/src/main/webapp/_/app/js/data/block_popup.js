define ([], function () {

    $_DO.update_block_popup = function (e) {

        var form = w2ui ['block_new_form']

        var v = form.values ()

        if (v.blocknum == null || v.blocknum == '') die ('blocknum', 'Укажите, пожалуйста, номер блока')
        if (!/[0-9А-ЯЁа-яёA-Za-z]/.test (v.blocknum)) die ('blocknum', 'Некорректный номер блока')

        if (parseFloat (v.totalarea || '0') < 0.01) die ('totalarea', 'Необходимо указать размер общей плошади')
        
        if (v.is_nrs == '0') {
            if (!v.code_vc_nsi_30) die ('code_vc_nsi_30', 'Необходимо указать характеристику помещения')
            if (parseFloat (v.grossarea || '0') < 0.01) die ('grossarea', 'Необходимо указать размер жилой плошади')
            if (!v.f_20002) die ('f_20002', 'Необходимо указать количество комнат')
        }        
        
        var tia = {type: 'blocks'}        
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        if (tia.action == 'create') {
/*
            var grid = w2ui ['house_premises_residental_grid']

            $.each (grid.records, function () {        
                if (this.terminationdate) return;
                if (this.blocknum != v.blocknum) return;            
                die ('blocknum', 'Помещение с таким заводским номером уже зарегистрировано')
            })
*/
            v.uuid_house = $_REQUEST.id

        }

        var enums = {
            f_20053: 1,
            f_20054: 1,
            f_20056: 1,
        }

        for (i in enums) {v [i] = v [i] ? [v [i]] : []}

        query (tia, {data: v}, reload_page)

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.get ('record')
        sessionStorage.removeItem ('record')

        done (data)

    }

})