define ([], function () {

    var form_name = 'premise_usage_tarif_common_form'

    $_DO.approve_premise_usage_tarif_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'premise_usage_tarifs', action: 'approve'}, {}, reload_page)
    }

    $_DO.alter_premise_usage_tarif_common = function (e) {
        if (!confirm('Открыть эту карточку на редактирование?')) return
        query({type: 'premise_usage_tarifs', action: 'alter'}, {}, reload_page)
    }

    $_DO.annul_premise_usage_tarif_common = function (e) {
        use.block('premise_usage_tarif_annul_popup')
    }

    $_DO.cancel_premise_usage_tarif_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'premise_usage_tarifs'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }

    $_DO.edit_premise_usage_tarif_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5 (data)

    }

    $_DO.update_premise_usage_tarif_common = function (e) {

        if (!confirm ('Сохранить изменения?')) return

        var f = w2ui [form_name]

        var v = f.values ()

        if (!v.name)
            die('name', 'Укажите, пожалуйста, наименование')

        if (!v.datefrom)
            die('datefrom', 'Укажите, пожалуйста, дату начала действия')

        if (v.datefrom > v.dateto)
            die('enddate', 'Дата окончания действия не может предшествовать дате начала')

        if (!v.price)
            die('price', 'Укажите, пожалуйста, величину')

        v.oktmo = v.oktmo || []

        query({type: 'premise_usage_tarifs', action: 'update'}, {data: v}, reload_page)

    }

    $_DO.delete_premise_usage_tarif_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'premise_usage_tarifs', action: 'delete'}, {}, reload_page)
    }

    $_DO.choose_tab_premise_usage_tarif_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('premise_usage_tarif_common.active_tab', name)

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

        data.active_tab = localStorage.getItem ('premise_usage_tarif_common.active_tab') || 'premise_usage_tarif_common_log'

        data.__read_only = 1

        data.item.selected_oktmo = data.oktmos.map(function (i) {
            return {
                id: i['vc_oktmo.id'],
                code: i['vc_oktmo.code'],
                text: i['vc_oktmo.code'] + ' ' + i['vc_oktmo.site_name']
            }
        })

        done (data)
    }

})