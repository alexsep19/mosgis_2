define ([], function () {

    var form_name = 'social_norm_tarif_common_form'

    $_DO.approve_social_norm_tarif_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'social_norm_tarifs', action: 'approve'}, {}, reload_page)
    }

    $_DO.alter_social_norm_tarif_common = function (e) {
        if (!confirm('Открыть эту карточку на редактирование?')) return
        query({type: 'social_norm_tarifs', action: 'alter'}, {}, reload_page)
    }

    $_DO.annul_social_norm_tarif_common = function (e) {
        use.block('social_norm_tarif_annul_popup')
    }

    $_DO.cancel_social_norm_tarif_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'social_norm_tarifs'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }

    $_DO.edit_social_norm_tarif_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5 (data)

    }

    $_DO.update_social_norm_tarif_common = function (e) {

        if (!confirm ('Сохранить изменения?')) return

        var f = w2ui [form_name]

        var v = f.values ()

        if (!v.name)
            die('name', 'Укажите, пожалуйста, наименование')

        if (!v.datefrom)
            die('datefrom', 'Укажите, пожалуйста, дату начала действия')

        if (v.datefrom > v.dateto)
            die('enddate', 'Дата окончания действия не может предшествовать дате начала')

        if (!v.price || !(0 <= v.price && v.price <= 10000000))
            die('price', 'Укажите, пожалуйста, величину от 0 до 9999999.999')

        v.oktmo = v.oktmo || []

        query({type: 'social_norm_tarifs', action: 'update'}, {data: v}, reload_page)

    }

    $_DO.delete_social_norm_tarif_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'social_norm_tarifs', action: 'delete'}, {}, reload_page)
    }

    $_DO.choose_tab_social_norm_tarif_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('social_norm_tarif_common.active_tab', name)

        use.block (name)

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.item.status_label = data.vc_gis_status [data.item.id_ctr_status]

        data.item.oktmo_list = data.oktmos.map((i) => {
            return {
                html: '<span title="' + i['vc_oktmo.site_name'] + '">' + i['vc_oktmo.code'] + '</span>'
            }
        })

        data.active_tab = localStorage.getItem ('social_norm_tarif_common.active_tab') || 'social_norm_tarif_common_log'

        data.__read_only = 1

        data.item.selected_oktmo = data.oktmos.map(function (i) {
            return {
                id: i['vc_oktmo.id'],
                code: i['vc_oktmo.code'],
                text: i['vc_oktmo.code'] + ' ' + i['vc_oktmo.site_name']
            }
        })

        data.item.err_text = data.item ['out_soap.err_text']

        done (data)
    }

})