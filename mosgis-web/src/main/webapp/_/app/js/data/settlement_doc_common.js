define ([], function () {

    var form_name = 'settlement_doc_common_form'

    $_DO.delete_settlement_doc_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'settlement_docs', action: 'delete'}, {}, reload_page)
    }

    $_DO.approve_settlement_doc_common = function (e) {
        if (!confirm ('Разместить эти данные в ГИС ЖКХ?')) return
        query ({type: 'settlement_docs', action: 'approve'}, {}, reload_page)
    }

    $_DO.alter_settlement_doc_common = function (e) {
        if (!confirm ('Открыть эту карточку на редактирование?')) return
        query ({type: 'settlement_docs', action: 'alter'}, {data: {}}, reload_page)
    }

    $_DO.annul_settlement_doc_common = function (e) {
        use.block ('settlement_doc_annul_popup')
    }

    $_DO.choose_tab_settlement_doc_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('settlement_doc_common.active_tab', name)

        use.block (name)

    }

    return function (done) {
        
        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('settlement_doc_common.active_tab') || 'settlement_doc_payments'

        data.__read_only = 1

        var it = data.item

        it.status_label     = data.vc_gis_status [it.id_sd_status]
        it.state_label      = data.vc_gis_status [it.id_sd_state]

        if (it.id_sd_status != 10) {
            if (it.id_sd_status != it.id_sd_status_gis) it.gis_status_label = data.vc_gis_status [it.id_sd_status_gis]
            it.gis_state_label  = data.vc_gis_status [it.id_sd_state_gis]
        }

        it.err_text = it ['out_soap.err_text']

        if (it.id_sd_status_gis == 110) it.is_annuled = 1

        if (it.isgratuitousbasis == 0 && it.other) it.isgratuitousbasis = -1

        done (data)

    }

})