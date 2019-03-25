define ([], function () {

    $_DO.update_legal_act_new = function (e) {

        var form = w2ui ['legal_act_new_form']

        var v = form.values ()
        v.uuid_org = $_USER.uuid_org

        if (!v.level_)          die ('level_', 'Укажите, пожалуйста, уровень')
        if (!v.code_vc_nsi_324) die('code_vc_nsi_324', 'Укажите, пожалуйста, вид документа')
        if (!v.name)            die('name', 'Укажите, пожалуйста, наименование документа')

        if (!v.approvedate) die ('approvedate', 'Укажите, пожалуйста, дату принятия органом государственной власти')
        if (!v.files)       die('files', 'Укажите, пожалуйста, документ')
        v.scope = v.scope || 0
        if(!v.oktmo) delete v.oktmo

        var file = get_valid_gis_file (v, 'files')

        if (!/\.pdf$/.test(file.name)) die('files', 'Некорректный формат файла. Прикрепите pdf')

        Base64file.upload(file, {
            type: 'legal_acts',
            data: v,
            onprogress: show_popup_progress(file.size),
            onloadend: function () {
                w2popup.close()
                var grid = w2ui ['legal_acts_grid']
                grid.reload(grid.refresh)
            }
        })
    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete ('record') || {scope: 0}

        done(data)
    }

})