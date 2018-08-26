define ([], function () {

    $_DO.update_premise_nonresidental_popup = function (e) {

        var form = w2ui ['premise_nonresidental_new_form']

        var v = form.values ()

        if (v.premisesnum == null || v.premisesnum == '') die ('premisesnum', 'Укажите, пожалуйста, номер помещения')
        if (!/[0-9А-ЯЁа-яёA-Za-z]/.test (v.premisesnum)) die ('premisesnum', 'Некорректный номер помещения')
        
        if (parseFloat (v.totalarea || '0') < 0.01) die ('totalarea', 'Необходимо указать размер общей плошади')

        var tia = {type: 'premises_nonresidental'}        
        tia.id = form.record.id
        tia.action = tia.id ? 'update' : 'create'
        
        if (tia.action == 'create') {

            var grid = w2ui ['house_premises_nonresidental_grid']

            $.each (grid.records, function () {        
                if (this.terminationdate) return;
                if (this.premisesnum != v.premisesnum) return;            
                die ('premisesnum', 'Помещение с таким заводским номером уже зарегистрировано')
            })

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