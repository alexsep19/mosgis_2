define ([], function () {

    $_DO.update_social_norm_tarif_new = function (e) {

        var form = w2ui ['social_norm_tarif_new_form']

        var v = form.values ()
        v.uuid_org = $_USER.uuid_org

        if (!v.name)            die('name', 'Укажите, пожалуйста, наименование')

        if (!v.datefrom)        die ('datefrom', 'Укажите, пожалуйста, дату начала действия')

        if (v.datefrom > v.dateto) die('enddate', 'Дата окончания действия не может предшествовать дате начала')

        if (!v.price)           die('price', 'Укажите, пожалуйста, величину')

        if (!v.oktmo)           die('oktmo', 'Укажите, пожалуйста, территорию действия')

        query({type: 'social_norm_tarifs', id: undefined, action: 'create'}, {data: v}, function (data) {

            w2popup.close()

            if (data.id)
                w2confirm('Документ зарегистрирован. Открыть его страницу в новой вкладке?').yes(function () {
                    openTab('/social_norm_tarif/' + data.id)
                })

            use.block ('social_norm_tarifs')

        })
    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = $_SESSION.delete ('record') || {}

        done(data)
    }

})