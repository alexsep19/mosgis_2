define ([], function () {

    var form_name = 'legal_act_common_form'

    $_DO.approve_legal_act_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'legal_acts', action: 'approve'}, {}, reload_page)
    }

    $_DO.alter_legal_act_common = function (e) {
        if (!confirm('Открыть эту карточку на редактирование?')) return
        query({type: 'legal_acts', action: 'alter'}, {}, reload_page)
    }

    $_DO.cancel_legal_act_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'legal_acts'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }

    $_DO.edit_legal_act_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5 (data)

    }

    $_DO.update_legal_act_common = function (e) {

        if (!confirm ('Сохранить изменения?')) return

        var f = w2ui [form_name]

        var v = f.values ()

        if (!v.code_vc_nsi_324)
            die('code_vc_nsi_324', 'Укажите, пожалуйста, вид документа')
        if (!v.name)
            die('name', 'Укажите, пожалуйста, наименование документа')

        if (!v.approvedate)
            die('approvedate', 'Укажите, пожалуйста, дату принятия органом государственной власти')

        v.scope = v.scope || 0

        if ((!v.oktmo || !v.oktmo.length) && v.scope) {
            die('oktmo', 'Укажите, пожалуйста, ОКТМО муниципальных образований')
        }
        v.oktmo = v.oktmo || []

        if (!v.files) {

            query({type: 'legal_acts', action: 'edit'}, {data: v}, reload_page)

        } else {

            var file = get_valid_gis_file(v, 'files')

            if (!/\.pdf$/.test(file.name)) die('files', 'Некорректный формат файла. Прикрепите pdf')

            Base64file.upload(file, {
                type: 'legal_acts',
                data: v,
                onprogress: show_popup_progress(file.size),
                onloadend: reload_page
            })
        }

    }

    $_DO.delete_legal_act_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'legal_acts', action: 'delete'}, {}, reload_page)
    }

    $_DO.choose_tab_legal_act_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('legal_act_common.active_tab', name)

        use.block (name)

    }

    $_DO.download_legal_act_common = function (e) {

        var box = w2ui [form_name].box

        function label(cur, max) {
            return String(Math.round(100 * cur / max)) + '%'
        }

        w2utils.lock(box, label(0, 1))

        download({
            type: 'legal_acts',
            id: $_REQUEST.id,
            action: 'download',
        }, {}, {

            onprogress: function (cur, max) {
                $('.w2ui-lock-msg').html('<br><br>' + label(cur, max))
            },

            onload: function () {
                w2utils.unlock(box)
            },
        })
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.item.status_label = data.vc_gis_status [data.item.id_ctr_status]

        data.item.oktmo_list = data.legal_act_oktmo.map((i) => {
            return {
                html: '<span title="' + i.site_name + '">' + i.code + '</span>'
            }
        })

        data.active_tab = localStorage.getItem ('legal_act_common.active_tab') || 'legal_act_common_log'

        data.__read_only = 1

        data.item.selected_oktmo = data.legal_act_oktmo.map(function (i) {
            return {
                id: i.id,
                code: i.code,
                text: i.code + ' ' + i.site_name
            }
        })

        done (data)
    }

})